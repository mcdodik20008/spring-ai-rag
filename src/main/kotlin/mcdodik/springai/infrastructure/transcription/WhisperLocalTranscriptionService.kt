package mcdodik.springai.infrastructure.transcription

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@Component
class WhisperLocalTranscriptionService : TranscriptionService {
    override fun transcribe(file: MultipartFile): String {
        val tmpInput = Files.createTempFile("audio", ".wav").toFile()
        val tmpText = File.createTempFile("output", ".txt")

        // Сохраняем временный файл
        file.transferTo(tmpInput)

        // ffmpeg convert -> whisper -> output.txt
        ProcessBuilder(
            "ffmpeg",
            "-i",
            tmpInput.absolutePath,
            "-ar",
            "16000",
            "-ac",
            "1",
            "-f",
            "wav",
            "input.wav",
        ).start().waitFor()
        ProcessBuilder("./main", "-m", "models/ggml-base.en.bin", "-f", "input.wav", "-otxt").start().waitFor()

        return tmpText.readText()
    }
}
