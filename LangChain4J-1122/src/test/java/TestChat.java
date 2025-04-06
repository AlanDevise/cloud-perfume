import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;

/**
 * @Filename: TestChat.java
 * @Package: PACKAGE_NAME
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2025年04月06日 15:25
 */

public class TestChat {

    /**
     * openAI测试
     */
    @Test
    public void test() {
        ChatLanguageModel model = OpenAiChatModel
                .builder()
                // .apiKey("demo")
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .modelName("gpt-4o-mini")
                .build();
        String answer = model.chat("你好，你是谁？");
        System.out.println(answer);
    }

    @Test
    public void test2() {
        ChatLanguageModel model = OllamaChatModel
                .builder()
                // 默认的接口
                .baseUrl("http://localhost:11434")
                .modelName("deepseek-r1:7b")
                .build();
        String answer = model.chat("你好，你是谁？");
        System.out.println(answer);
    }
}
