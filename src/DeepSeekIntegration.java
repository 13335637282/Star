import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeepSeekIntegration {

    private static final Log log = new Log();
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    public static ArrayList<String> getResponse(String role, String content,String API_KEY) {
        // 构建请求体
        String requestBody = """
                {
                    "model": "deepseek-chat",
                    "messages": [
                        {
                            "role": "%s",
                            "content": "%s"
                        }
                    ]
                }
                """.formatted(
                        role
                                .replace("\n","\\n")
                                .replace("\"","\\\""),
                        content
                                .replace("\"","\\\"")
                                .replace("\n","\\n"));

        log.AddLog(Log.INFO,"[DeepSeekIntegration/getResponse] 请求体生成完成:"+requestBody);
        ArrayList<String> return_list = new ArrayList<>();

        try {
            HttpClient httpClient = HttpClients.createDefault();

            HttpPost httpPost = new HttpPost(API_URL);
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + API_KEY);

            // 设置请求体
            StringEntity entity = new StringEntity(requestBody);
            httpPost.setEntity(entity);

            // 执行请求
            HttpResponse response = httpClient.execute(httpPost);

            // 获取响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // 获取响应内容
                String responseBody = EntityUtils.toString(response.getEntity());
                return_list.add("200");
                return_list.add(responseBody);
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                return_list.add(statusCode+"");
                return_list.add(responseBody);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.AddLog(Log.INFO,"[DeepSeekIntegration/getResponse] 收到返回:"+return_list);
        return return_list;
    }
}