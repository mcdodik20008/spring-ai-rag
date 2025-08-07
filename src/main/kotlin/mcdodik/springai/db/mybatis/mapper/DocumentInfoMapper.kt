package mcdodik.springai.db.mybatis.mapper

import java.util.UUID
import mcdodik.springai.db.model.DocumentInfo
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * Mapper interface for interacting with the DocumentInfo database table using MyBatis.
 * Provides methods for CRUD operations and advanced searches on document metadata.
 */
@Mapper
interface DocumentInfoMapper {

    /**
     * Retrieves all documents stored in the database.
     *
     * @return A list of [DocumentInfo] objects representing all documents.
     */
    fun findAll(): List<DocumentInfo>

    /**
     * Retrieves a document by its unique identifier.
     *
     * @param id The unique ID of the document.
     * @return The [DocumentInfo] object corresponding to the given ID, or null if not found.
     */
    fun findById(id: UUID): DocumentInfo?

    /**
     * Inserts a new document into the database.
     *
     * @param documentInfo The [DocumentInfo] object to be inserted.
     */
    fun insert(documentInfo: DocumentInfo)

    /**
     * Retrieves a document by its file name.
     *
     * @param fileName The name of the file associated with the document.
     * @return The [DocumentInfo] object corresponding to the given file name.
     */
    fun findByFileName(fileName: String): DocumentInfo

    /**
     * Searches for a document by both file name and hash.
     * Useful for checking if a document with the same content already exists.
     *
     * @param fileName The name of the file associated with the document.
     * @param hash The hash of the document's content.
     * @return The [DocumentInfo] object if found, or null if no matching document exists.
     */
    fun searchByNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String
    ): DocumentInfo?

    /**
     * Retrieves the unique ID of a document by both file name and hash.
     * This is useful when only the ID is needed without loading the full document info.
     *
     * @param fileName The name of the file associated with the document.
     * @param hash The hash of the document's content.
     * @return The UUID of the document if found, or null if no matching document exists.
     */
    fun getIdByFileNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String
    ): UUID?

    /**
     * Deletes a document from the database by its unique identifier.
     *
     * @param id The unique ID of the document to delete.
     */
    fun delete(id: UUID)
}
