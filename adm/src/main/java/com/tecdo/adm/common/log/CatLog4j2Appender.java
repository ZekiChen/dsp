package com.tecdo.adm.common.log;

import com.dianping.cat.Cat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

@Plugin(name = "CatLog4j2Appender", category = "Core", elementType = "appender", printObject = true)
public class CatLog4j2Appender extends AbstractAppender {

  public CatLog4j2Appender(String name,
                           Filter filter,
                           Layout<? extends Serializable> layout,
                           boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions);
  }

  private void logError(LogEvent event) {
    ThrowableProxy info = event.getThrownProxy();
    if (info != null) {
      Throwable exception = info.getThrowable();

      Object message = event.getMessage();
      if (message != null) {
        Cat.logError(String.valueOf(message), exception);
      } else {
        Cat.logError(exception);
      }
    }
  }

  private void logTrace(LogEvent event) {
    String type = "Log4j2";
    String name = event.getLevel().toString();
    Object message = event.getMessage();
    String data;
    if (message instanceof Throwable) {
      data = buildExceptionStack((Throwable) message);
    } else {
      data = event.getMessage().toString();
    }

    ThrowableProxy info = event.getThrownProxy();
    if (info != null) {
      data = data + '\n' + buildExceptionStack(info.getThrowable());
    }

    Cat.logTrace(type, name, "0", data);
  }

  private String buildExceptionStack(Throwable exception) {
    if (exception != null) {
      StringWriter writer = new StringWriter(2048);
      exception.printStackTrace(new PrintWriter(writer));
      return writer.toString();
    } else {
      return "";
    }
  }

  @Override
  public void append(LogEvent event) {
    try {
      boolean isTraceMode = Cat.getManager().isTraceMode();
      Level level = event.getLevel();
      if (level.isMoreSpecificThan(Level.ERROR)) {
        logError(event);
      } else if (isTraceMode) {
        logTrace(event);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @PluginFactory
  public static CatLog4j2Appender createAppender(@PluginAttribute("name") String name,
                                                 @PluginElement("Filter") final Filter filter,
                                                 @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                 @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
    if (name == null) {
      LOGGER.error("no name defined in conf.");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new CatLog4j2Appender(name, filter, layout, ignoreExceptions);
  }

}
