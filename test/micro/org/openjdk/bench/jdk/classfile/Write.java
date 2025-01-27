/*
 * Copyright (c) 2022, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.bench.jdk.classfile;

import java.lang.reflect.AccessFlag;
import java.lang.classfile.ClassFile;
import java.lang.classfile.attribute.SourceFileAttribute;
import org.openjdk.jmh.annotations.*;
import java.io.FileOutputStream;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STATIC;
import static java.lang.constant.ConstantDescs.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.classfile.Opcode.*;
import static java.lang.classfile.TypeKind.*;
import static org.openjdk.bench.jdk.classfile.TestConstants.*;

/**
 * Write
 *
 * Generates this program with 40 mains...
 *
 * class MyClass {
 *   public static void main(String[] args) {
 *     int fac = 1;
 *     for (int i = 1; i < 10; ++i) {
 *       fac = fac * i;
 *     }
 *     System.out.println(fac);
 *   }
 * }
 */
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1, jvmArgs = {
        "--add-exports", "java.base/jdk.internal.classfile.impl=ALL-UNNAMED"})
public class Write {
    static final int REPEATS = 40;
    static final String[] METHOD_NAMES;
    static {
        var names = new String[REPEATS];
        for (int xi = 0; xi < REPEATS; ++xi) {
            names[xi] = "main" + ((xi == 0) ? "" : "" + xi);
        }
        METHOD_NAMES = names;
    }
    static String checkFileBc = "/tmp/byw/MyClass.class";
    static boolean writeClassBc = Files.exists(Paths.get(checkFileBc).getParent());

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public byte[] jdkTree() {

        byte[] bytes = ClassFile.of().build(CD_MyClass, cb -> {
            cb.withFlags(AccessFlag.PUBLIC);
            cb.withVersion(52, 0);
            cb.with(SourceFileAttribute.of(cb.constantPool().utf8Entry(("MyClass.java"))))
              .withMethod(INIT_NAME, MTD_void, 0, mb -> mb
                      .withCode(codeb -> codeb.aload(0)
                                              .invokespecial(CD_Object, INIT_NAME, MTD_void)
                                              .return_()
                      )
              );
            for (int xi = 0; xi < REPEATS; ++xi) {
                cb.withMethod(METHOD_NAMES[xi], MTD_void_StringArray,
                              ACC_PUBLIC | ACC_STATIC,
                              mb -> mb.withCode(c0 -> {
                                  java.lang.classfile.Label loopTop = c0.newLabel();
                                  java.lang.classfile.Label loopEnd = c0.newLabel();
                                  int vFac = 1;
                                  int vI = 2;
                                  c0.iconst_1()         // 0
                                    .istore(vFac)       // 1
                                    .iconst_1()         // 2
                                    .istore(vI)         // 3
                                    .labelBinding(loopTop)
                                    .iload(vI)          // 4
                                    .bipush(10)         // 5
                                    .if_icmpge(loopEnd) // 6
                                    .iload(vFac)        // 7
                                    .iload(vI)          // 8
                                    .imul()             // 9
                                    .istore(vFac)       // 10
                                    .iinc(vI, 1)        // 11
                                    .goto_(loopTop)     // 12
                                    .labelBinding(loopEnd)
                                    .getstatic(CD_System, "out", CD_PrintStream) // 13
                                    .iload(vFac)
                                    .invokevirtual(CD_PrintStream, "println", MTD_void_int) // 15
                                    .return_();
                        }));
            }
        });
        if (writeClassBc) writeClass(bytes, checkFileBc);
        return bytes;
    }

    private void writeClass(byte[] bytes, String fn) {
        try {
            FileOutputStream out = new FileOutputStream(fn);
            out.write(bytes);
            out.close();
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }
}

