本目录用于存放用户头像（持久化，重启后端后仍保留）。

目录结构：
  data/uploads/avatars/

使用方式：
1. 小程序/Web 上传：文件自动保存到此目录，数据库 user.avatar 记录访问路径。
2. 手动放置默认头像（可选）：
   - 将图片命名为 {用户ID}.jpg 或 {学号用户名}.jpg
   - 例如：1.jpg、20210001.png
   - 当数据库 avatar 为空时，系统会自动从此目录读取并展示。

访问地址：
  http://localhost:8080/uploads/avatars/文件名

配置项（backend/src/main/resources/application.properties）：
  app.upload-dir=../data/uploads
