package com.aichat.roleplay.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aichat.roleplay.model.Role;

@Slf4j
@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final ResourceLoader resourceLoader;
    private final IRoleService roleService;
    private final DocumentSplitter documentSplitter = DocumentSplitters.recursive(300, 50);

    // 角色维度的embedding存储
    private final Map<Long, EmbeddingStore<TextSegment>> roleEmbeddingStores = new ConcurrentHashMap<>();

    // 角色ID到角色名称的映射
    private final Map<String, String> roleNameMapping = new ConcurrentHashMap<>();

    @Autowired
    public RagService(EmbeddingModel embeddingModel, ResourceLoader resourceLoader, IRoleService roleService) {
        this.embeddingModel = embeddingModel;
        this.resourceLoader = resourceLoader;
        this.roleService = roleService;
    }

    @PostConstruct
    public void initializeRagDocuments() {
        log.info("开始初始化RAG文档...");

        try {
            // 从数据库获取所有公开角色
            List<Role> allRoles = roleService.getAllPublicRoles();
            log.info("从数据库获取到 {} 个角色", allRoles.size());

            for (Role role : allRoles) {
                try {
                    // 动态确定文档目录名称
                    String documentDirectory = determineDocumentDirectory(role);
                    if (documentDirectory != null) {
                        loadDocumentsForRole(String.valueOf(role.getId()), documentDirectory);
                        log.info("成功为角色 '{}' (ID: {}) 加载RAG文档，目录: {}",
                                role.getName(), role.getId(), documentDirectory);
                    } else {
                        log.info("角色 '{}' (ID: {}) 没有对应的RAG文档目录",
                                role.getName(), role.getId());
                    }
                } catch (Exception e) {
                    log.warn("为角色 '{}' (ID: {}) 加载RAG文档时出错: {}",
                            role.getName(), role.getId(), e.getMessage());
                }
            }

            log.info("RAG文档初始化完成，共处理 {} 个角色", allRoles.size());
        } catch (Exception e) {
            log.error("RAG文档初始化失败", e);
        }
    }



    /**
     * 为指定角色加载文档
     */
    private void loadDocumentsForRole(String roleId, String docDirName) {
        try {
            // 构建角色文档目录路径
            String roleDocPath = "classpath:rag-documents/" + docDirName + "/";

            // 尝试加载角色目录下的所有文档
            Resource roleResource = resourceLoader.getResource(roleDocPath);

            // 检查资源是否存在
            if (!roleResource.exists()) {
                log.warn("角色 {} (ID: {}) 的文档目录不存在: {}", docDirName, roleId, roleDocPath);
                return;
            }

            // 获取文档目录的实际路径
            Path roleDir = Paths.get(roleResource.getURI());

            if (!Files.exists(roleDir) || !Files.isDirectory(roleDir)) {
                log.warn("角色 {} (ID: {}) 的文档目录不存在或不是目录: {}", docDirName, roleId, roleDir);
                return;
            }

            // 遍历目录下的所有.txt文件
            try (Stream<Path> files = Files.walk(roleDir)) {
                List<Path> txtFiles = files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".txt"))
                        .collect(Collectors.toList());

                if (txtFiles.isEmpty()) {
                    log.warn("角色 {} (ID: {}) 的文档目录中没有找到.txt文件", docDirName, roleId);
                    return;
                }

                // 为角色创建embedding store
                EmbeddingStore<TextSegment> roleStore = new InMemoryEmbeddingStore<>();
                roleEmbeddingStores.put(Long.valueOf(roleId), roleStore);

                int documentCount = 0;
                for (Path txtFile : txtFiles) {
                    try {
                        String content = Files.readString(txtFile, StandardCharsets.UTF_8);
                        if (!content.trim().isEmpty()) {
                            addDocumentToRole(roleId, content, txtFile.getFileName().toString());
                            documentCount++;
                        }
                    } catch (IOException e) {
                        log.error("读取角色 {} 的文档文件 {} 失败: {}", docDirName, txtFile, e.getMessage());
                    }
                }

                if (documentCount > 0) {
                    log.info("成功为角色 {} (ID: {}) 加载了 {} 个文档", docDirName, roleId, documentCount);
                } else {
                    log.warn("角色 {} (ID: {}) 没有成功加载任何文档", docDirName, roleId);
                }

            }

        } catch (Exception e) {
            log.error("为角色 {} (ID: {}) 加载文档失败: {}", docDirName, roleId, e.getMessage());
        }
    }

    /**
     * 为指定角色添加文档
     */
    public void addDocumentToRole(String roleId, String text, String documentId) {
        try {
            log.info("为角色 {} 添加文档: {}", roleId, documentId);

            Long roleIdLong = Long.valueOf(roleId);
            EmbeddingStore<TextSegment> roleStore = roleEmbeddingStores.get(roleIdLong);
            if (roleStore == null) {
                roleStore = new InMemoryEmbeddingStore<>();
                roleEmbeddingStores.put(roleIdLong, roleStore);
            }

            // 创建文档并分割
            Document document = Document.from(text);
            List<TextSegment> segments = documentSplitter.split(document);

            // 为每个片段生成embedding并存储
            for (TextSegment segment : segments) {
                Embedding embedding = embeddingModel.embed(segment).content();
                roleStore.add(embedding, segment);
            }

            log.info("角色 {} 文档添加成功，共 {} 个片段", roleId, segments.size());
        } catch (Exception e) {
            log.error("为角色 {} 添加文档失败: {}", roleId, e.getMessage());
        }
    }

    /**
     * 为指定角色搜索相关内容
     */
    public List<String> searchRelevantContentForRole(String roleId, String query, int maxResults) {
        try {
            log.info("为角色 {} 搜索相关内容: {}", roleId, query);

            EmbeddingStore<TextSegment> roleStore = roleEmbeddingStores.get(roleId);
            if (roleStore == null) {
                log.warn("角色 {} 没有文档存储", roleId);
                return List.of();
            }

            // 生成查询的embedding
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // 搜索相似内容
            List<EmbeddingMatch<TextSegment>> matches = roleStore.findRelevant(queryEmbedding, maxResults, 0.7);

            return matches.stream()
                    .map(match -> match.embedded().text())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("角色 {} 搜索失败: {}", roleId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 为指定角色构建RAG提示词
     */
    public String buildRagPromptForRole(String roleId, String question, int maxResults) {
        List<String> relevantContent = searchRelevantContentForRole(roleId, question, maxResults);

        if (relevantContent.isEmpty()) {
            return question;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下相关信息回答问题：\n\n");

        for (int i = 0; i < relevantContent.size(); i++) {
            prompt.append("参考资料 ").append(i + 1).append(":\n");
            prompt.append(relevantContent.get(i)).append("\n\n");
        }

        prompt.append("问题: ").append(question).append("\n");
        prompt.append("请基于上述参考资料回答问题，如果参考资料中没有相关信息，请说明。");

        return prompt.toString();
    }

    // 兼容原有接口的方法
    public void addDocument(String text, String documentId) {
        // 默认添加到通用存储
        addDocumentToRole("default", text, documentId);
    }

    public List<String> searchRelevantContent(String query, int maxResults) {
        // 默认从通用存储搜索
        return searchRelevantContentForRole("default", query, maxResults);
    }

    public String buildRagPrompt(String question, int maxResults) {
        // 默认使用通用存储
        return buildRagPromptForRole("default", question, maxResults);
    }

    public String getRoleRelevantContent(Long roleId, String query, int maxResults) {
        try {
            // 添加详细的调试信息
            log.info("开始检索角色 {} 的相关内容，查询: {}", roleId, query);
            log.info("当前roleEmbeddingStores包含的角色ID: {}", roleEmbeddingStores.keySet());

            EmbeddingStore<TextSegment> store = roleEmbeddingStores.get(roleId);
            if (store == null) {
                log.warn("角色 {} 没有对应的知识库", roleId);
                return "";
            }

            // 检查store中的文档数量
            log.info("角色 {} 的知识库存在，开始embedding查询", roleId);

            Embedding queryEmbedding = embeddingModel.embed(query).content();
            List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(queryEmbedding, maxResults, 0.7);

            log.info("角色 {} 的embedding查询完成，找到 {} 个匹配项", roleId, matches.size());

            if (matches.isEmpty()) {
                // 尝试降低相似度阈值
                matches = store.findRelevant(queryEmbedding, maxResults, 0.3);
                log.info("降低阈值后，角色 {} 找到 {} 个匹配项", roleId, matches.size());

                if (matches.isEmpty()) {
                    log.debug("角色 {} 没有找到相关内容，查询: {}", roleId, query);
                    return "";
                }
            }

            StringBuilder relevantContent = new StringBuilder();
            for (EmbeddingMatch<TextSegment> match : matches) {
                log.debug("匹配项相似度: {}, 内容: {}", match.score(), match.embedded().text().substring(0, Math.min(50, match.embedded().text().length())));
                relevantContent.append(match.embedded().text()).append("\n");
            }

            log.debug("角色 {} 找到 {} 条相关内容", roleId, matches.size());
            return relevantContent.toString().trim();

        } catch (Exception e) {
            log.error("获取角色相关内容失败: roleId={}, query={}", roleId, query, e);
            return "";
        }
    }

    /**
     * 获取角色相关内容（默认返回3条结果）
     */
    public String getRoleRelevantContent(Long roleId, String query) {
        return getRoleRelevantContent(roleId, query, 3);
    }

    /**
     * 动态确定角色的文档目录名称
     * 支持多种命名策略，完全基于角色属性动态匹配
     */
    private String determineDocumentDirectory(Role role) {
        String roleName = role.getName();
        String category = role.getCategory();

        // 获取所有可用的文档目录
        Set<String> availableDirectories = getAvailableDocumentDirectories();

        // 策略1: 直接名称匹配（英文名）
        String directMatch = findDirectMatch(roleName, availableDirectories);
        if (directMatch != null) {
            return directMatch;
        }

        // 策略2: 名称转换匹配（中文转英文）
        String translatedMatch = findTranslatedMatch(roleName, availableDirectories);
        if (translatedMatch != null) {
            return translatedMatch;
        }

        // 策略3: 基于分类的模糊匹配
        String categoryMatch = findCategoryMatch(category, availableDirectories);
        if (categoryMatch != null) {
            return categoryMatch;
        }

        // 策略4: 关键词匹配
        String keywordMatch = findKeywordMatch(roleName, availableDirectories);
        if (keywordMatch != null) {
            return keywordMatch;
        }

        return null; // 没有找到匹配的目录
    }

    /**
     * 获取所有可用的RAG文档目录
     */
    private Set<String> getAvailableDocumentDirectories() {
        Set<String> directories = new HashSet<>();
        try {
            Resource ragResource = resourceLoader.getResource("classpath:rag-documents");
            if (ragResource.exists()) {
                java.io.File ragDir = ragResource.getFile();
                java.io.File[] subdirs = ragDir.listFiles(java.io.File::isDirectory);
                if (subdirs != null) {
                    for (java.io.File subdir : subdirs) {
                        directories.add(subdir.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取RAG文档目录列表时出错: {}", e.getMessage());
        }
        return directories;
    }

    /**
     * 直接名称匹配
     */
    private String findDirectMatch(String roleName, Set<String> availableDirectories) {
        // 尝试直接匹配（忽略大小写）
        for (String dir : availableDirectories) {
            if (dir.equalsIgnoreCase(roleName)) {
                return dir;
            }
        }
        return null;
    }

    /**
     * 名称转换匹配（中文角色名转英文目录名）
     */
    private String findTranslatedMatch(String roleName, Set<String> availableDirectories) {
        // 常见的中英文对照
        Map<String, String> nameTranslations = new HashMap<>();
        // 历史人物和虚拟角色
        nameTranslations.put("哈利波特", "harry-potter");
        nameTranslations.put("哈利·波特", "harry-potter");
        nameTranslations.put("Harry Potter", "harry-potter");
        nameTranslations.put("苏格拉底", "socrates");
        nameTranslations.put("Socrates", "socrates");
        nameTranslations.put("爱因斯坦", "einstein");
        nameTranslations.put("Einstein", "einstein");
        nameTranslations.put("阿尔伯特·爱因斯坦", "einstein");
        nameTranslations.put("Albert Einstein", "einstein");
        nameTranslations.put("孔子", "confucius");
        nameTranslations.put("Confucius", "confucius");
        nameTranslations.put("佛陀", "buddha");
        nameTranslations.put("Buddha", "buddha");
        nameTranslations.put("释迦牟尼", "buddha");
        nameTranslations.put("莎士比亚", "william-shakespeare");
        nameTranslations.put("威廉·莎士比亚", "william-shakespeare");
        nameTranslations.put("William Shakespeare", "william-shakespeare");
        nameTranslations.put("达芬奇", "leonardo-da-vinci");
        nameTranslations.put("列奥纳多·达·芬奇", "leonardo-da-vinci");
        nameTranslations.put("Leonardo da Vinci", "leonardo-da-vinci");
        nameTranslations.put("亚当斯密", "adam_smith");
        nameTranslations.put("亚当·斯密", "adam_smith");
        nameTranslations.put("Adam Smith", "adam_smith");
        
        // 职业角色
        nameTranslations.put("产品经理", "product-manager");
        nameTranslations.put("Product Manager", "product-manager");
        nameTranslations.put("前端开发", "frontend-developer");
        nameTranslations.put("前端工程师", "frontend-developer");
        nameTranslations.put("Frontend Developer", "frontend-developer");
        nameTranslations.put("后端开发", "backend-developer");
        nameTranslations.put("后端工程师", "backend-developer");
        nameTranslations.put("Backend Developer", "backend-developer");
        nameTranslations.put("运维工程师", "devops-engineer");
        nameTranslations.put("DevOps工程师", "devops-engineer");
        nameTranslations.put("DevOps Engineer", "devops-engineer");

        String translated = nameTranslations.get(roleName);
        if (translated != null && availableDirectories.contains(translated)) {
            return translated;
        }

        return null;
    }

    /**
     * 基于分类的模糊匹配
     */
    private String findCategoryMatch(String category, Set<String> availableDirectories) {
        if (category == null) return null;

        // 根据分类推断可能的目录
        Map<String, List<String>> categoryMappings = new HashMap<>();
        categoryMappings.put("历史人物", Arrays.asList("socrates", "einstein"));
        categoryMappings.put("虚拟角色", Arrays.asList("harry-potter"));
        categoryMappings.put("科学家", Arrays.asList("einstein"));
        categoryMappings.put("哲学家", Arrays.asList("socrates"));
        categoryMappings.put("魔法师", Arrays.asList("harry-potter"));

        List<String> possibleDirs = categoryMappings.get(category);
        if (possibleDirs != null) {
            for (String dir : possibleDirs) {
                if (availableDirectories.contains(dir)) {
                    return dir;
                }
            }
        }

        return null;
    }

    /**
     * 关键词匹配
     */
    private String findKeywordMatch(String roleName, Set<String> availableDirectories) {
        // 基于角色名称中的关键词进行匹配
        String lowerName = roleName.toLowerCase();

        for (String dir : availableDirectories) {
            String lowerDir = dir.toLowerCase();

            // 检查是否包含相关关键词
            if (containsRelevantKeywords(lowerName, lowerDir)) {
                return dir;
            }
        }

        return null;
    }

    /**
     * 检查是否包含相关关键词
     */
    private boolean containsRelevantKeywords(String roleName, String directoryName) {
        // 定义关键词映射
        Map<String, List<String>> keywordMappings = new HashMap<>();
        keywordMappings.put("harry", Arrays.asList("哈利", "波特", "魔法", "霍格沃茨"));
        keywordMappings.put("potter", Arrays.asList("哈利", "波特", "魔法", "霍格沃茨"));
        keywordMappings.put("socrates", Arrays.asList("苏格拉底", "哲学", "古希腊", "智慧"));
        keywordMappings.put("einstein", Arrays.asList("爱因斯坦", "物理", "相对论", "科学"));

        for (Map.Entry<String, List<String>> entry : keywordMappings.entrySet()) {
            String keyword = entry.getKey();
            List<String> relatedTerms = entry.getValue();

            if (directoryName.contains(keyword)) {
                for (String term : relatedTerms) {
                    if (roleName.contains(term)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}