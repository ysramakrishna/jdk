/*
 * Copyright (c) 1997, 2025, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef SHARE_OOPS_TYPEARRAYKLASS_HPP
#define SHARE_OOPS_TYPEARRAYKLASS_HPP

#include "oops/arrayKlass.hpp"

class ClassLoaderData;

// A TypeArrayKlass is the klass of a typeArray
// It contains the type and size of the elements

class TypeArrayKlass : public ArrayKlass {
  friend class Deoptimization;
  friend class oopFactory;
  friend class VMStructs;

 public:
  static const KlassKind Kind = TypeArrayKlassKind;

 private:
  jint _max_length;            // maximum number of elements allowed in an array

  // Constructor
  TypeArrayKlass(BasicType type, Symbol* name);
  static TypeArrayKlass* allocate_klass(ClassLoaderData* loader_data, BasicType type, Symbol* name, TRAPS);

  typeArrayOop allocate_common(int length, bool do_zero, TRAPS);
  typeArrayOop allocate_instance(int length, TRAPS) { return allocate_common(length, true, THREAD); }
 public:
  TypeArrayKlass() {} // For dummy objects.

  // instance variables
  jint max_length()                     { return _max_length; }
  void set_max_length(jint m)           { _max_length = m;    }

  u2 compute_modifier_flags() const;

  // testers
  DEBUG_ONLY(bool is_typeArray_klass_slow() const  { return true; })

  // klass allocation
  static TypeArrayKlass* create_klass(BasicType type, const char* name_str,
                               TRAPS);
  static TypeArrayKlass* create_klass(BasicType type, TRAPS) {
    return create_klass(type, external_name(type), THREAD);
  }

  size_t oop_size(oop obj) const;

  // Allocation
  oop multi_allocate(int rank, jint* sizes, TRAPS);

  oop protection_domain() const { return nullptr; }

  // Copying
  void  copy_array(arrayOop s, int src_pos, arrayOop d, int dst_pos, int length, TRAPS);

  // Oop iterators. Since there are no oops in TypeArrayKlasses,
  // these functions only return the size of the object.

 private:
  // The implementation used by all oop_oop_iterate functions in TypeArrayKlasses.
  inline void oop_oop_iterate_impl(oop obj, OopIterateClosure* closure);

 public:
  // Wraps oop_oop_iterate_impl to conform to macros.
  template <typename T, typename OopClosureType>
  inline void oop_oop_iterate(oop obj, OopClosureType* closure);

  // Wraps oop_oop_iterate_impl to conform to macros.
  template <typename T, typename OopClosureType>
  inline void oop_oop_iterate_bounded(oop obj, OopClosureType* closure, MemRegion mr);

  // Wraps oop_oop_iterate_impl to conform to macros.
  template <typename T, typename OopClosureType>
  inline void oop_oop_iterate_reverse(oop obj, OopClosureType* closure);

 public:
  static TypeArrayKlass* cast(Klass* k) {
    return const_cast<TypeArrayKlass*>(cast(const_cast<const Klass*>(k)));
  }

  static const TypeArrayKlass* cast(const Klass* k) {
    assert(k->is_typeArray_klass(), "cast to TypeArrayKlass");
    return static_cast<const TypeArrayKlass*>(k);
  }

  // Naming
  static const char* external_name(BasicType type);

  // Sizing
  static int header_size()  { return sizeof(TypeArrayKlass)/wordSize; }
  int size() const          { return ArrayKlass::static_size(header_size()); }

  // Initialization (virtual from Klass)
  void initialize(TRAPS);

 public:
  // Printing
  void oop_print_on(oop obj, outputStream* st);
  void oop_print_elements_on(typeArrayOop ta, outputStream* st);
  void print_on(outputStream* st) const;
  void print_value_on(outputStream* st) const;

 public:
  const char* internal_name() const;

  ModuleEntry* module() const;
  PackageEntry* package() const;
};

#endif // SHARE_OOPS_TYPEARRAYKLASS_HPP
