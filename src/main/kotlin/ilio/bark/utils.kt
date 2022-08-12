package ilio.bark

fun wrapUnit(block: () -> Any?) {
    block()
}

fun Boolean.doIf(block: () -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}

fun Boolean.doElse(block: () -> Unit): Boolean {
    if (!this) {
        block()
    }
    return this
}
