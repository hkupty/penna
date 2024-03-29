---
title: "Why Penna?"
date: 2023-06-05T22:16:44+02:00
description: "Featherweight JSON Logging for the JVM"
tags:
    - slf4j
    - logging
    - java
---

## Why?
Back in January this year I started asking myself this question: "Can I make logging better?".
Well, better is a fun word, as it seams to mean so much, and yet nothing concrete at the same time.

<!--more-->

Better, in this case, meant "specialized". I wanted structured logging, only. Json logs by default.

I mean, it is 2023 after all. I've been using loki in production for many months with great success, and have seen
the wonders that could be achieved by using the elastic stack and splunk before loki.

I felt that it was necessary to get something out of my system, a draft or anything, so I started.

## Setting up expectations and goals

One of the first things I wrote, before anything, was a document laying down the foundational principles of Penna, well, Maple actually. More on that later..

I quickly established what I wanted it to have and what I felt was unnecessary.

This has proven to be fundamental and looking back is what I believe made me stick to the core values from the begining to have a quick answer to the questions I had, while at the same time a solid foundation to rely on when making decisions.

It was obviously starting from the "Structured Logging" requirement. A natural follow-up was being easy to set up, with sane defaults.

Later, the more I got involved with it, the more it made sense for me to pursue another interesting feature: Unobtrusiveness.

## Hyped about Java 17

Another interesting thought that always seemed to revolve around my head was how modern JVM features seem to be postponed or put aside in projects because there's a strong hold-back in the java community to upgrading to newer JVM versions.

Seeing things like records, gc improvements, pattern matching and many other exciting features pop up while I felt they would take ages to reach mainstream apps and libraries.

It felt natural to target Java 17 then, when starting the project.

This would pair up nicely with the requirement of being unobtrusive.

## What the heck is unobtrusive?

As a non-native english speaker, I completely understand the question.

In the context of Penna, I mean simply that it should not stand in the way of your application.

It can mean many different things, in different contexts. For example, with the newest slf4j api, we can log key-value pairs of data. While that is supported for logback, at the time I was fiddling with this idea, it wasn't supported by logback-logstash-encoder. There, the logger got in the way.

Another thing it can mean that it should be fast, or not slow the application down. Logback does a remarkable job in being fast, though performance can mean different things for different people. Yet, I felt this could be another thing where this log library could excel. Reducing the memory pressure by being smart, lean and reusing a few resources, would definitely reduce the impact the logger could have.

A third point worth mentioning is this: writing json automatically to stdout, without any need to third-party dependencies, is in fact a way of being unobtrusive. By not imposing any dependency requirement on the client, giving it the freedom to chose whichever (or even none at all) json libraries, not worrying about classpath clashes or anything, is another strong selling point. I reckon it wasn't always like this, but thankfully I was [enlightened by brilliant feedback I got on reddit by agentoutlier](https://www.reddit.com/r/java/comments/11nqjqx/comment/jbstyky/?utm_source=share&utm_medium=web2x&context=3), among others, that definitely aligned with the project principles.

## Maple or Penna?

Initially, this was project was called maple. I felt it was a name that would convey the meaning in a playful way, as it is at the same time robust, valuable and sweet. But as a known fact in the software industry, naming is hard and great ideas are usually already taken. There was already a [java logger for structured logging named Maple.](https://github.com/Randgalt/maple)
Kudos to Randgalt for being clever first!

So I once again looked back at the principles. Structure and Light. That's a feather. Also, Penna in many languages is a pen or a pencil, something used by the ancient to.. write. That's perfect!

## Releasing something concrete

Having set a name, Penna development was full speed ahead. Versions 0.5 and 0.6 were released focusing on delivering on its promise:

### Lightweight configuration

Penna ships with a sane set of defaults. Logging json to stdout is granted, `INFO` is usually the log level we go by when something runs in production and there's a few fields that should always be present. So, just to get something started, there's no need to add anything other then Penna to your classpath and you're good to go.

### Structured logging

There's so much context around a simple `logger.info` statement that should never go to waste. It can be particularly helpful when one is debugging or trying to find correlations between behavior and log messages. Beign able to filter, index and process logs without concerning about positional field placement, for example, is immensely beneficial.
This is why Penna

### Unobtrusiveness

Penna is developed to have a lean internal structure; It reuses a few core objects to avoid memory allocation and GC and writes to stdout using low-level abstractions to try to squeeze the most performance possible.

For in-detail description of each improvement step, have a look at [the changelog](https://hkupty.github.io/penna/changelog/)

## Next steps

Currently, Penna is heading to its 0.7 release that promises to be even better: native MDC implementation, faster json keyword writing, in-place string formatting and many other features to make it better suited to all usecases.

I hope you are as thrilled as I am with Penna now that I shared a brief story about its inception, ideas, reason and goals!
Please consider helping out by testing it, submitting pull-requests, opening issues or discussions or even [sponsoring this project](https://github.com/sponsors/hkupty).
Github interaction is important because it signals me that this project is valuable and the sponsoring allows me to put off time to exclusively focus on Penna.

Best regards,
Henry
