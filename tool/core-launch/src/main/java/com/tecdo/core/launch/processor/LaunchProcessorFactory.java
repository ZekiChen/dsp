package com.tecdo.core.launch.processor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/9
 */
public class LaunchProcessorFactory {

    private final Iterator<ILauncherProcessor> iterator;

    private LaunchProcessorFactory() {
        List<ILauncherProcessor> processors = new ArrayList<>();
        ServiceLoader.load(ILauncherProcessor.class).forEach(processors::add);
        List<ILauncherProcessor> sortProcessors = processors.stream()
                .sorted(Comparator.comparing(ILauncherProcessor::getOrder))
                .collect(Collectors.toList());
        iterator = sortProcessors.iterator();
    }

    private static final class LaunchProcessorFactoryHolder {
        static final LaunchProcessorFactory INSTANCE = new LaunchProcessorFactory();
    }

    public static LaunchProcessorFactory getSingleton() {
        return LaunchProcessorFactoryHolder.INSTANCE;
    }

    public ILauncherProcessor getLaunchProcessor() {
        return iterator.next();
    }

    public boolean hasNextProcessor() {
        return iterator.hasNext();
    }

}
