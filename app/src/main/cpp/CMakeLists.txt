cmake_minimum_required(VERSION 3.22.1)
project(premiumscrypt C)

add_library(premiumscrypt SHARED scrypt_jni.c)
target_compile_options(premiumscrypt PRIVATE -O3)
