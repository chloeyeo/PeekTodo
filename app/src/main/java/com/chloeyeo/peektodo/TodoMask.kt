package com.chloeyeo.peektodo

private const val MASK_CHAR = "•" // •
private const val MASK_MIN = 3
private const val MASK_MAX = 24

/** Replaces a to-do title with dots of roughly the same length so masked lists keep their shape. */
fun maskTodoTitle(title: String): String = MASK_CHAR.repeat(title.length.coerceIn(MASK_MIN, MASK_MAX))
