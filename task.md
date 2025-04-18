# Thread Pool - Задание на курсовую работу

## Общее описание курсовой работы

Задание по курсовой работе включает в себя разработку кода (один или несколько файлов), написание демонстрационной программы, а также подготовку мини-отчета.

Отчет оформляется в свободной форме (писать непосредственно в `README.md`-файле).

Решение должно быть представлено в виде ссылки на публичный GitHub репозиторий.

## Легенда

Предполагаем, что ведется работа над высоконагруженным серверным приложением, в котором важно управлять распределением задач (запросов) между потоками. Для обеспечения гибкости и эффективности было принято решение не использовать стандартный `ThreadPoolExecutor`, а написать собственный пул потоков с настраиваемым управлением очередями, логированием, параметрами и политикой отказа.

## Основные требования

### Параметры настройки пула

- `corePoolSize` — минимальное (базовое) количество потоков.
- `maxPoolSize` — максимальное количество потоков.
- `keepAliveTime` и `timeUnit` — время, в течение которого поток может простаивать до завершения и единицы измерения этого самого времени.
- `queueSize` — ограничение на количество задач в очереди.
- `minSpareThreads` — минимальное число «резервных» потоков, которые должны быть всегда доступны. Если число свободных потоков падает ниже этого значения, пул должен создавать новые потоки, даже при невысокой нагрузке.

### Обработка отказов

При переполнении очереди и загруженности всех потоков необходимо реализовать механизм отказа (например, с использованием `RejectedExecutionHandler`), который определяет, как поступать с новой задачей (отклонять, выполнять в текущем потоке или применять иной способ обработки). В отчете обязательно следует указать, почему ,был выбран именно этот подход и какие могут быть его недостатки.

### Распределение задач

Также необходимо реализовать алгоритм балансировки задач - например, поступающие задачи могут распределяться по принципу `Round Robin` между несколькими очередями, привязанными к разным рабочим потокам.

## Кастомизация компонентов

- `ThreadFactory` — необходимо разработать фабрику для создания потоков, которая будет присваивать потокам уникальные имена и логировать события их создания и завершения.
- `Очереди задач` — можно использовать несколько стандартных `BlockingQueue` (по одной на каждый поток).

### Worker (рабочий поток) должен:

- Обрабатывать задачи из закрепленной за ним очереди.
- При отсутствии задач в течение времени, превышающего `keepAliveTime`, завершаться, если общее число потоков превышает `corePoolSize`.
Перед выполнением новой задачи проверять, что пул не находится в завершенном состоянии (`shutdown`).

### Логирование

Необходимо реализовать подробное логирование, которое покрывает все ключевые этапы работы системы.

Примеры сообщений (формат вывода можно изменить, главное - содержание) могут быть следующими:

- При создании каждого потока: `[ThreadFactory] Creating new thread: MyPool-worker-1`.
- При завершении потока: `[Worker] MyPool-worker-1 terminated`.
- При поступлении задачи: `[Pool] Task accepted into queue #(id): <описание задачи>`.
- Если очередь переполнена / задача отклонена `[Rejected] Task <...> was rejected due to overload`.
- При выполнении каждой задачи: `[Worker] MyPool-worker-2 executes <описание задачи>`.
- При `idle timeout` (воркер не получил задачу за `keepAliveTime`): `[Worker] MyPool-worker-2 idle timeout, stopping`.

Важно, чтобы логи фиксировали все существенные события (создание/завершение потоков, постановка задач в очередь, выполнение задачи, отказ в обработке и таймаут бездействия) и позволяли однозначно понять состояние системы в любой момент.

### Интерфейс управления

Разрабатываемый пул потоков должен реализовывать следующий интерфейс:

```java
interface CustomExecutor extends Executor {
   void execute(Runnable command);
    <T> Future<T> submit(Callable<T> callable);
    void shutdown();
    void shutdownNow();
}
```

Это позволит использовать пул как для работы с `CompletableFuture`, так и напрямую через методы `execute` и `submit`.

### Демонстрационная программа

Необходимо написать тестовый класс (например, `Main`), где:

- Инициализируется пул с выбранными параметрами (например, `corePoolSize=2`, `maxPoolSize=4`, `queueSize=5`, `keepAliveTime=5` секунд).
- В пул отправляется несколько «имитационных» задач (например, реализация `Runnable`, которая делает `Thread.sleep(...)` и логирует начало и окончание выполнения).
- После некоторого времени вызывается метод `shutdown()` и проверяется, что все задачи завершены, а потоки корректно освобождены.
- Демонстрируется обработка ситуации, когда поступает слишком много задач (например, задачи отклоняются или обрабатываются согласно заданной политике).

### Отчет

Нужно предоставить отчет (можно в `README.md`-файле), в котором в свободной форме будут изложены:

- `Анализ производительности` — сравнение вашего пула с пулами из стандартной библиотеки либо с аналогами (например, `Tomcat`, `Jetty` и пр.).
- `Мини-исследование` того, какие значения различных параметров пула приводят к максимальным показателям производительности.
- `Принцип действия` механизма распределения задач между очередями и работы балансировки (если реализованы несколько очередей и алгоритм балансировки) — должен быть объяснен вкратце, подробно расписывать не нужно.

## Формат сдачи работы

В результате работы должны получиться:

- Код разрабатываемого `ThreadPool`.
- Отчет.
- Демонстрационная программа.
