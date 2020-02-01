# keeper
keeper for team running!

## 1. 定时周报发送

1. 督促team成员发送周报，否则会发送remind邮件
2. 简化编写成本，通过 **issue template** & **markdown** 语法，专注于周报内容本身
3. 统一管理主题、收件人等信息
4. 归档管理周报信息，追踪周报中的问题

![weekly diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/wangyuheng/keeper/master/.plantuml/weekly.puml)

## 2. 成员负载可视化

通过图表直观展示团队成员工作负载、贡献、以及项目进度等信息
核心目的: 细化工作量, 通过可量化、可追踪的方式, 把控项目进度

高价值指标包括: 

1. 成员当前issue数量
2. 当前版本项目issue进度及分布
3. 成员参与issue数量
4. bug数量趋势

![workload diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/wangyuheng/keeper/master/.plantuml/workload.puml)
