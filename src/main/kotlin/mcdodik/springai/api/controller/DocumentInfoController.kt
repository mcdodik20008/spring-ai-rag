package mcdodik.springai.api.controller

import java.util.UUID
import mcdodik.springai.api.service.DocumentInfoService
import mcdodik.springai.db.entity.rag.DocumentInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing document information entities.
 * Provides endpoints for retrieving and deleting document data.
 */
@RestController
@RequestMapping("/api/documents")
class DocumentInfoController(
    /**
     * Service used to interact with the document information data source.
     */
    private val service: DocumentInfoService
) {

    /**
     * Retrieves all document information records.
     *
     * @return A list of [DocumentInfo] objects representing all stored documents.
     */
    @GetMapping
    fun getAll(): List<DocumentInfo> = service.getAll()

    /**
     * Retrieves a specific document by its unique identifier.
     *
     * @param id The UUID of the document to retrieve.
     * @return The [DocumentInfo] object corresponding to the provided ID.
     */
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): DocumentInfo =
        service.getById(id)

    /**
     * Retrieves a specific document by its file name.
     *
     * @param fileName The name of the file associated with the document.
     * @return The [DocumentInfo] object corresponding to the provided file name.
     */
    @GetMapping("/by-file-name")
    fun getById(@RequestParam fileName: String): DocumentInfo =
        service.getByFileName(fileName)

    /**
     * Deletes a document information record by its unique identifier.
     *
     * @param id The UUID of the document to delete.
     * @return A [ResponseEntity] with HTTP status 204 (NO_CONTENT) upon successful deletion.
     */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
