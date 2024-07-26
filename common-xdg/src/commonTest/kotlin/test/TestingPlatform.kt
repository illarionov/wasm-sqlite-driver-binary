/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EMPTY_PRIMARY_CONSTRUCTOR")

package ru.pixnews.wasm.sqlite.open.helper.common.xdg.test

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreJs()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreWasmJs()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreMingw()

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public expect annotation class IgnoreApple()
