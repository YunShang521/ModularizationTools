package com.modularization.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

public class ModularizationPlugin implements Plugin<Project> {
    String compilemodule = "app";

    @Override
    public void apply(Project project) {
        List<String> taskNames =  project.getGradle().getStartParameter().getTaskNames();
        System.out.println("taskNames--------- " +taskNames.toString());
        System.out.println(" project.getPath -------- " +  project.getPath());
        String module = project.getPath().replace(":", "");
        System.out.println("module -------- " + module);
        CurrTask currTask = getTaskInfo(taskNames);

        //对于isRunAlone==true的情况需要根据实际情况修改其值，
        // 但如果是false，则不用修改，该module作为一个lib，运行module:assembleRelease则发布aar到中央仓库
        boolean isApplaction = true;

        String mainmodulename= project.getRootProject().property("main.module.name").toString();
        System.out.println("mainmodulename--------- " + mainmodulename);

        //对于要编译的组件和主项目，isRunAlone修改为true，其他组件都强制修改为false
        //这就意味着组件不能引用主项目，这在层级结构里面也是这么规定的
        if ((!module.equals(mainmodulename))&&(currTask.isUpload || (taskNames.toString().contains("assemble")&& !taskNames.toString().contains(project.getPath())))) {
            isApplaction = false;
        } else {
            isApplaction = true;
        }
        System.out.println("isApplaction--------- " + isApplaction);
        project.getExtensions().add("isApplaction",isApplaction);
        if (isApplaction) {
            project.getPluginManager().apply("com.android.application");
            System.out.println("apply plugin is com.android.application");
        }else{
            project.getPluginManager().apply("com.android.library") ;
            System.out.println("apply plugin is com.android.library");
        }

    }


    private CurrTask getTaskInfo(List<String> taskNames) {
        CurrTask assembleTask = new CurrTask();
        for (String task : taskNames) {
            if (task.toUpperCase().contains("UPLOAD")||
                    task.toUpperCase().contains("ARCHIVUES")||
                    task.toUpperCase().contains("UA")||
                    task.toUpperCase().contains("UPA")
            ) {
                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true;
                }
                assembleTask.isUpload = true;
                String[] strs = task.split(":");
                assembleTask.modules.add(strs.length > 1 ? strs[strs.length - 2] : "all");
                break;
            }
        }
        return assembleTask;
    }


    /**
     * 当前task 属性封装
     */
    private class CurrTask {
        boolean isAssemble = false;
        boolean isDebug = false;
        boolean isUpload = false;
        List<String> modules = new ArrayList<>();
    }

}
