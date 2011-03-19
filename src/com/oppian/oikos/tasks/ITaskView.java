package com.oppian.oikos.tasks;

import com.oppian.oikos.OikosManager;

public interface ITaskView {
    public void success(int resId);
    public void error(int resId);
    public OikosManager getManager();
}
