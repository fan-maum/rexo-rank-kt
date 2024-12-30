# LexoRank on Kotlin
This is a ported project of [lexorank-ts](https://github.com/kvandake/lexorank-ts) that can be used in Kotlin.

## Getting Started
Gradle (Kotlin)
```kotlin
implementation("com.fanmaum:LexoRankKt:1.0.0")
```

Gradle (Groovy)
```groovy
implementation 'com.fanmaum:LexoRankKt:1.0.0'
```

## Using

### Static methods
```kotlin
// min
val minLexoRank = LexoRank.min()
// max
val maxLexoRank = LexoRank.max()
// middle
val middleLexoRank = LexoRank.middle()
// parse
val parsedLexoRank = LexoRank.parse("0|0i0000:")
```

### Public methods
```kotlin
// any lexoRank
val lexoRank = LexoRank.middle()

// generate next lexorank
val nextLexoRank = lexoRank.genNext()

// generate previous lexorank
val prevLexoRank = lexoRank.genPrev()

// toString
val lexoRankStr = lexoRank.toString()
```

### Calculate LexoRank
LexRank calculation based on existing LexoRanks.
```kotlin
// any lexorank
val any1LexoRank = LexoRank.min()

// another lexorank
val any2LexoRank = any1LexoRank.genNext().genNext()

// calculate between
val betweenLexoRank = any1LexoRank.between(any2LexoRank)
```

## Related projects
- [lexorank-ts](https://github.com/kvandake/lexorank-ts)

## Licence
```
Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0.txt
```
