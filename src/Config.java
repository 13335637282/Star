import com.google.gson.*;

import javax.imageio.IIOException;
import javax.swing.*;
import java.io.*;

public class Config {
    static File file = new File("Star/config.json");
    static File asset_path = new File("Star");
    public Config() {
        try {
            if (!file.isFile()) {
                file.delete();
            }
            if (!file.exists()) {
                asset_path.mkdirs();
                file.createNewFile();
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
                fileWriter.write("""
{
  "log": {
    "path": "Star/log"
  },
  "settings": {
    "lang": "zh_cn",
    "ui": {
      "theme": "dark",
      "os": "mac"
    },
    "font": "微软雅黑"
  },
  "test": {
    "last_test": ""
  },

  "lang": {
    "zh_cn": {
      "select_test_paper": "选择试卷",
      "start_test": "开始考试",
      "settings": "设置",
      "submit": "提交",
      "test_interface": "测试界面",
      "current_test_paper": "当前试卷: ",
      "time": "时间: ",
      "submit_test_paper_msg": "提交试卷完成!\n核对结果：\n 总空数: %d\n 正确数: %d\n 正确率: %f%%\n得分情况\n 得分: %d\n 总分: %d\n 得分占比:%f%%",
      "question_number": "第%d题答案",
      "star_test_paper_file": "Star 试卷文件",
      "error_blank": "此空无法正确识别，请核对版本和文件内容",
      "error": "无法正确识别，请核对版本和文件内容",
      "play_listening": "播放听力",
      "return_main_interface": "返回主界面",
      "reading_test_failed": "读取试卷失败",
      "no_select_test_paper": "请先选择测试试卷",
      "playback_failed": "播放失败",
      "correct": "正确",
      "incorrect": "不正确",
      "result": "结果",
      "test_error": "无法正确识别该试卷，请核对版本和文件内容。\n如果选择\"无视风险，继续\"，后续出现的风险，产品概不负责。",
      "ignore_risks": "无视风险，继续"
    },
    "en_us": {
      "select_test_paper": "Select Test Paper",
      "start_test": "Start Test",
      "settings": "Settings",
      "submit": "Submit",
      "test_interface": "Test Interface",
      "current_test_paper": "Current Test Paper: ",
      "time": "Time: ",
      "submit_test_paper_msg": "Test paper submitted!\nResults verification:\n Total blanks: %d\n Correct: %d\n Accuracy: %f%%\nScore\n Score: %d\n Total score: %d\n Score percentage: %f%%",
      "question_number": "Answer for question %d",
      "star_test_paper_file": "Star Test Paper File",
      "error_blank": "This blank cannot be recognized, please check version and file content",
      "error": "Cannot be recognized, please check version and file content",
      "play_listening": "Play Listening",
      "return_main_interface": "Return to Main Interface",
      "reading_test_failed": "Failed to read test paper",
      "no_select_test_paper": "Please select a test paper first",
      "playback_failed": "Playback failed",
      "correct": "Correct",
      "incorrect": "Incorrect",
      "result": "Result",
      "test_error": "This test paper cannot be correctly recognized. Please check the version and file content.\nIf you choose \"Ignore Risks and Continue\", the product will not be responsible for any subsequent risks.",
      "ignore_risks": "Ignore Risks and Continue"
    }
  }
}
                        """);
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setString(String path, String value) {
        setValue(path, new JsonPrimitive(value));
    }

    public void setBoolean(String path, boolean value) {
        setValue(path, new JsonPrimitive(value));
    }

    public void setFloat(String path, float value) {
        setValue(path, new JsonPrimitive(value));
    }

    public void setJsonArray(String path, JsonArray value) {
        setValue(path, value);
    }

    public void setLong(String path, long value) {
        setValue(path, new JsonPrimitive(value));
    }

    public void setInt(String path, int value) {
        setValue(path, new JsonPrimitive(value));
    }

    public String getLang(String path) {
        return getString("lang." +
                getString("settings.lang")+
                "."+path);
    }

    private void setValue(String path, JsonElement value) {
        try {
            // 读取现有配置
            StringBuilder fileText = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.lines().forEachOrdered(fileText::append);
            reader.close();

            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(fileText.toString()).getAsJsonObject();

            // 分割路径并导航到目标位置
            String[] keys = path.split("\\.");
            JsonObject current = root;

            // 导航到父对象
            for (int i = 0; i < keys.length - 1; i++) {
                String key = keys[i];
                if (!current.has(key) || !current.get(key).isJsonObject()) {
                    current.add(key, new JsonObject());
                }
                current = current.getAsJsonObject(key);
            }

            // 设置值
            String lastKey = keys[keys.length - 1];
            current.add(lastKey, value);

            // 写回文件
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(root));
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getString(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String return_value;
        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }
            return_value = element.getAsString();
        } catch (Exception e) {
            return "";
        }
        return return_value;
    }

    public Boolean getBoolean(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        boolean return_value;

        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }

            return_value = element.getAsBoolean();

        } catch (Exception e) {
            return null;
        }

        return return_value;
    }

    public Double getDouble(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        double return_value;

        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }

            return_value = element.getAsDouble();

        } catch (Exception e) {
            return null;
        }

        return return_value;
    }

    public Float getFloat(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        float return_value;

        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }

            return_value = element.getAsFloat();

        } catch (Exception e) {
            return null;
        }

        return return_value;
    }

    public int getInt(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int return_value;

        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }

            return_value = element.getAsInt();

        } catch (Exception e) {
            return 0;
        }

        return return_value;
    }

    public long getLong(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        long return_value;

        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }

            return_value = element.getAsLong();

        } catch (Exception e) {
            return 0;
        }

        return return_value;
    }

    public JsonArray getJsonArray(String path) {
        JsonParser gson = new JsonParser();
        StringBuilder file_text = new StringBuilder();
        try {
            new BufferedReader(new FileReader(file)).lines().forEachOrdered(file_text::append);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        JsonArray return_value;

        try {
            JsonElement element = gson.parse(file_text.toString());
            for (String i : path.split("[.]")) {
                element = element.getAsJsonObject().get(i);
            }

            return_value = element.getAsJsonArray();

        } catch (Exception e) {
            return null;
        }

        return return_value;
    }
}