package mcdodik.springai.api.controller

import java.util.UUID
import mcdodik.springai.api.service.DocumentInfoService
import mcdodik.springai.db.model.DocumentInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/documents")
class DocumentInfoController(
    private val service: DocumentInfoService
) {

    @GetMapping
    fun getAll(): List<DocumentInfo> = service.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): DocumentInfo =
        service.getById(id)

    @GetMapping("/by-file-name")
    fun getById(@RequestParam fileName: String): DocumentInfo =
        service.getByFileName(fileName)

//    @PostMapping
//    fun create(@RequestBody doc: DocumentInfo): ResponseEntity<DocumentInfo> {
//        val created = service.create(doc)
//        return ResponseEntity.status(HttpStatus.CREATED).body(created)
//    }

//    @PutMapping("/{id}")
//    fun update(@PathVariable id: UUID, @RequestBody doc: DocumentInfo): DocumentInfo =
//        service.update(id, doc)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
