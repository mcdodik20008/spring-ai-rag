package mcdodik.springai.rag.application

import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.rag.api.SummaryService

class DbSummaryService(
    private val mapper: DocumentInfoMapper,
) : SummaryService {
    override fun summariesByFileName(fileNames: Set<String>): Map<String, String> {
        if (fileNames.isEmpty()) return emptyMap()
        return mapper.searchByFilenames(fileNames).associateBy({ it.fileName }, { it.summary ?: "" })
    }
}
