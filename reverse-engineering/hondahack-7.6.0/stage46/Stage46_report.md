# Stage 46 — Manifest 组件生命周期与通信矩阵

## 方法
将 Stage 30 的 Manifest 组件—DEX 类映射与 Stage 45 的 Intent/Bundle API 调用点按精确类描述符连接，统计每个组件类中的通信 API。

## 结果
- 交叉引用组件：27
- Activity：23
- Service：2
- Receiver：1
- Provider：1
- 组件类中存在 Intent/Bundle API 调用：12
- 其中 Activity：10，Service：1，Receiver：1

## 输出
- component_communication_matrix.tsv
- stage46_summary.json

## 限制
仅按精确组件类描述符连接；helper 类、回调及跨类调用不归属到组件；不解析 Intent 目标和生命周期可达路径；运行时注册、隐式解析、反射与 native 路径未建模。
