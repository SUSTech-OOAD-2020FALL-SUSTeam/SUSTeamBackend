package susteam.storage

import java.nio.file.Path

typealias FileStorage = io.vertx.ext.web.FileUpload

val FileStorage.uploadName
    get() = this.uploadedFileName()

val FileStorage.fileName
    get() = this.fileName()

val FileStorage.suffix
    get() = Path.of(this.fileName).toFile().extension

val FileStorage.uuid
    get() = Path.of(this.uploadName).toFile().name

val FileStorage.isImage
    get() = this.contentType().startsWith("image")
