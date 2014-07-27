package com.arcao.mobilemtk.ui;

import com.arcao.mobilemtk.ui.activity.ActivityModule;
import com.arcao.mobilemtk.ui.fragment.FragmentModule;
import com.arcao.mobilemtk.ui.task.TaskModule;

import dagger.Module;

@Module(
				includes = {
								ActivityModule.class,
								FragmentModule.class,
								TaskModule.class
				},
				complete = false,
				library = true
)
public final class UiModule {

}

