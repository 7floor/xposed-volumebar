package com.sevenfloor.xposedvolumebar;

import android.app.Dialog;
import android.content.res.XModuleResources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Module implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private static final String PATHCED_PACKAGE = "android.microntek.service";
    private static String MODULE_PATH = null;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PATHCED_PACKAGE))
            return;

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        resparam.res.setReplacement(PATHCED_PACKAGE, "layout", "volume_dialog", modRes.fwd(R.layout.volume_dialog));
        resparam.res.setReplacement(PATHCED_PACKAGE, "layout", "volume_slider", modRes.fwd(R.layout.volume_slider));
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals(PATHCED_PACKAGE))
            return;

        findAndHookMethod("com.microntek.app.VolumeDialog", loadPackageParam.classLoader, "onCreate", Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Dialog dlg = (Dialog) param.thisObject;
                        Window wnd = dlg.getWindow();
                        wnd.setBackgroundDrawable(new ColorDrawable(0xE0101010));
                        WindowManager.LayoutParams lp = wnd.getAttributes();
                        lp.gravity = Gravity.BOTTOM;
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        wnd.setAttributes(lp);
                    }
                });
    }
}
