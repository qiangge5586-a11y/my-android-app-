# Stage 47 — 线程、异步执行与服务调度索引

## 方法
扫描 Thread/Runnable、Executor/Future、Handler/Looper/Message、AsyncTask、Timer、wait/notify、Service 生命周期、JobScheduler/JobService 与 WorkManager API。

## 结果
- 扫描语义文件：1540
- 匹配调用点：529
- 涉及调用类：150

### 分类计数
- handler_looper：157
- thread：122
- executor：58
- timer：65
- thread_sleep_wait：41
- async_task：82
- service_lifecycle：4

## 输出
- async_thread_api_sites.tsv
- async_thread_class_summary.tsv
- stage47_summary.json

## 限制
API 引用不证明运行时执行；未解析线程创建与任务提交、回调配对、竞态、happens-before 或锁归属；框架与 native 调度路径不覆盖。
