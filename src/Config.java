import com.google.gson.*;

import javax.imageio.IIOException;
import javax.swing.*;
import java.io.*;

public class Config {
    static File file = new File(".Star/config.json");
    static File asset_path = new File(".Star");
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
                              "path": ".Star/log"
                            },
                            "settings": {
                              "lang": "zh_cn",
                              "ui": {
                                "theme": "light",
                                "os": "windows"
                              }
                            },
                            "test": {
                              "last_test": ""
                            },
                            "lang": {
                              "zh_cn": {
                                "select_test_paper": "选择试卷",
                                "start_test": "开始考试",
                                "settings": "设置",
                                "multiple_choice": "多选题",
                                "single_choice": "单选题",
                                "fill_in_the_blank": "填空题",
                                "application": "应用题",
                                "essay": "作文",
                                "true_or_false": "判断题",
                                "submit": "提交",
                                "current_test_paper": "当前试卷: "
                              },
                              "en": {
                                "select_test_paper": "Select Test Paper",
                                "start_test": "Start Test",
                                "settings": "Settings",
                                "multiple_choice": "Multiple Choice Question",
                                "single_choice": "Single Choice Question",
                                "fill_in_the_blank": "Fill In The Blank Question",
                                "application": "Application Question",
                                "essay": "Essay",
                                "true_or_false": "True Or False Question",
                                "submit": "Submit",
                                "current_test_paper": "Current Test Paper: "
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