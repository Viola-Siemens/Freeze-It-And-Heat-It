### Task 2: 创建 package-info.java

**Files:**
- Create: `src/main/java/com/hexagram2021/fiahi/common/event/package-info.java`

**Interfaces:**
- Produces: 包声明文件

- [ ] **Step 1: 创建 package-info.java**

```java
/**
 * FIAHI 事件包喵~
 *
 * 包含 FIAHI 暴露给附属模组的事件类喵~
 *
 * @author liudongyu
 */
@NonNullByDefault
package com.hexagram2021.fiahi.common.event;

import org.jetbrains.annotations.Nonnull;
import org.jetbrains.annotations.NonnullByDefault;
```

- [ ] **Step 2: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交 package-info**

```bash
git add src/main/java/com/hexagram2021/fiahi/common/event/package-info.java
git commit -m "docs: 添加 event 包的 package-info.java"
```

---

