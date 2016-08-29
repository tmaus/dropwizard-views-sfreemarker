package com.pirack.dropwizard.freemarker

import com.google.common.cache.{CacheLoader, CacheBuilder, LoadingCache}
import freemarker.template.Configuration
import freemarker.template.Version
import io.dropwizard.views.View
import io.dropwizard.views.ViewRenderer
import scala.collection.JavaConversions._
import java.util
import java.io.{OutputStream, OutputStreamWriter}
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Locale


/**
  * Created by tmaus on 26/8/16.
  *
  * @see https://github.com/dropwizard/dropwizard/blob/master/dropwizard-views-freemarker/src/main/java/io/dropwizard/views/freemarker/FreemarkerViewRenderer.java
  */
class SFreemarkerViewRenderer extends ViewRenderer{
  val FREEMARKER_VERSION = Configuration.getVersion()
  val loader:TemplateLoader = new TemplateLoader(FREEMARKER_VERSION)
  val configurationCache:LoadingCache[Class[_], Configuration] = CacheBuilder.newBuilder()
    .concurrencyLevel(128).build(loader)

  override def configure(options: util.Map[String, String]): Unit = loader.setBaseConfig(options)

  override def render(view: View, locale: Locale, output: OutputStream): Unit = {
    val configuration = configurationCache.getUnchecked(view.getClass);
    val charset = view.getCharset().orElse(Charset.forName(configuration.getEncoding(locale)))
    val template = configuration.getTemplate(view.getTemplateName(), locale, charset.name());
    template.process(view, new OutputStreamWriter(output, template.getEncoding()));
  }

  override def isRenderable(view:View) : Boolean = view.getTemplateName.endsWith(getSuffix)
  override def getSuffix : String = ".ftl"
}

object ScalaFreemarkerViewRenderer {

}

class TemplateLoader(freemarkerVersion:Version) extends CacheLoader[Class[_], Configuration]{
  var baseConfig: util.Map[String,String] = null
  override def load(key: Class[_]): Configuration = {
    val configuration = new Configuration(freemarkerVersion)
    configuration.setObjectWrapper(new ScalaObjectWrapper(freemarkerVersion))
    configuration.loadBuiltInEncodingMap()
    configuration.setDefaultEncoding(StandardCharsets.UTF_8.name())
    configuration.setClassForTemplateLoading(key, "/")
    for( (k,v) <- baseConfig) configuration.setSetting(k,v)
    configuration
  }

  def setBaseConfig(baseConfig:util.Map[String, String]) = this.baseConfig = baseConfig
}


