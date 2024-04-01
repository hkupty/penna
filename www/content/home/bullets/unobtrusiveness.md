---
title: "Unobtrusiveness"
date: 2023-04-21T16:05:18+02:00
icon: flash
build:
    render: never
---

The logging framework should not draw out much attention. It should just work. With that in mind, penna tries to be a simple yet effective component in your architecture.
It should not require you to add in more dependencies. Instead, it should work with whatever you have available.

Also, it should make its best effort to consume the fewer resources as possible, being efficient and sparing your app of GC pauses when under heavy load.
[Read more on our performance tests.](https://github.com/hkupty/penna/tree/dev/0.7/performance)
