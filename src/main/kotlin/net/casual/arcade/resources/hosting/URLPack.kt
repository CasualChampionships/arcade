package net.casual.arcade.resources.hosting

import org.apache.commons.io.FilenameUtils
import java.io.InputStream
import java.net.URL

public class URLPack(
    private val url: URL
): ReadablePack {
    override val name: String
        get() = FilenameUtils.getName(this.url.path)

    override fun stream(): InputStream {
        return this.url.openStream()
    }
}