package net.casual.arcade.resources

import org.apache.commons.io.FilenameUtils
import java.io.InputStream
import java.net.URL

class URLPack(
    private val url: URL
): ReadablePack {
    override val name: String
        get() = FilenameUtils.getName(this.url.path)

    override fun stream(): InputStream {
        return this.url.openStream()
    }
}