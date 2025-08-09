package mcdodik.springai.infrastructure.multipart

import org.springframework.web.multipart.MultipartFile

interface MultipartFileFactory<T> {
    /** Может ли фабрика обработать именно этот объект? */
    fun supports(input: Any): Boolean

    /** Преобразование уже гарантированно подходящего объекта */
    fun create(input: T): MultipartFile
}
