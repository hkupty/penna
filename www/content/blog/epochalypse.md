---
title: "Epochalypse: Is your logger safe against Y2038?"
date: 2025-02-04T22:27:57+01:00
description: "Are we ready for Y2038?"
tags:
    - java
    - security
---

## Wait, what?

The other day I came across [this great youtube video by Brodie Robertson](https://www.youtube.com/watch?v=_AEyyCRtBVA) about the Y2038 problem and I realized 2038 is not that far and, he's right, we need to do something about it while we can.

Well, if you happened to land on this page without context, I'll shortly explain what this is about in the next sessions, don't worry.

Before we dive in, I want to get one thing out of the way: Penna is designed to be Y2038-safe.
Just today I added tests to ensure we won't regress on it and from now on, there's no shadow of doubt — it's safe. This is good.

Ok, back to the [year 2038 problem](https://www.epochalypse.today/). The birds-eye view into it is simple: If you store dates in a short format, in 2038-01-19, at 03:14:08 your clock will overflow and you'll be back to 1901-12-13. Or, at least, some part of your system will think that.

<!-- more -->

This is because dates stored or manipulated using signed 32-bit integers will overflow.

I can hear someone already saying "but isn't Java using Instants and Dates and all the fancy types for handling time correctly?". Yes, that is correct. Luckily that's a solved problem already and outside the scope of what we're talking about.
There's another format of date that is frequently used and _is_ represented as a number: _timestamps_.

Fear not, Penna uses _long_ for storing timestamps, providing 32 extra bits to ensure timestamps won't overflow anytime soon — or even in the distant future. It is that simple, yes.

## If it's that simple, why care?

The Y2K38 bug (yes, it has many names) is a "time bomb bug", the one kind that we can't see in action until certain time has passed. This means that it might be silently lurking through our applications and services until it's too late to fix them. And in that video, Brodie pointed out an excellent point: libraries in use today might
not be updated for a long time. There's a significant chance that versions we release today will stay as they are in software considered stable for a long time. So, the sooner we fix our libraries and make our code safer, the higher the chance we have to mitigate Y2038 bugs in a forthcoming future.

There's another aspect of that which makes me believe that this is the right thing to care about now: It's the compounding effect. I could have just made sure to verify the code was safe and go on with my life, but I think that by echoing the message and continuing talking about it, more people will take action.
Of course it would be pretentious of me to assume I have a bigger audience than a youtuber with almost 100k subscribers. But written content has staying power. As Penna grows, so will the visibility of this issue, ensuring more developers take action.

It is inevitably an investment to a safer future which will yield some return, and I, at least at the time of writing this, cannot foresee it yet. And, as I wrote in [why I believe in open source]({{<ref "/blog/why-I-believe-in-open-source.md" >}} "Why I Believe in Open Source"), this is OSS. It is an environment for the big and the small.

## Closing thoughts

This is a shorter than usual post, I just wanted to highlight this important security feature of Penna and keep the message alive for the next to come. So aside from the usual high-performance structured logging, you can also count that Penna is future-proof.

Penna is an independent project dedicated to structured JSON logging with top-notch performance. Your support helps ensure continued development, hardening and future-proofing. If you find value in this, consider [sponsoring the project](https://github.com/sponsors/hkupty) or sharing your thoughts in our [discussions](https://github.com/hkupty/penna/discussions).
