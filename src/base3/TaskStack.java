package base3;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

public class TaskStack {

    public Map<String, CmdTask> taskMap;
    public Map<String, Integer> holdMap;
    TaskStack cla;
    public TaskStackExe exeTask;

    public TaskStack(int periodMs) {
        cla = this;
        taskMap = new HashMap<String, CmdTask>();
        holdMap = new HashMap<String, Integer>();
        Timer timer = new Timer(); // Creating a Timer object from the timer class
        TimerTask task1 = new TimerTask() {
            public void run() {
                for (String key : taskMap.keySet()) {
                    CmdTask task = taskMap.get(key);
                    if (task.retryTim < task.retryDly) {
                        task.retryTim++;
                        continue;
                    }
                    if (task.stepTim < task.stepDly) {
                        task.stepTim++;
                        continue;
                    }
                    task.stepTim = 0;
                    exeTask.exe(task);
                }
            }
        };
        timer.schedule(task1, 0, periodMs); // Using the schedule method of the timer class
    }

    public CmdTask addTaskStrA(String[] strA) {
        CmdTask task=this.addTask(strA[0], 1, 50, 0);
        for(int i=1;i<strA.length;i++)
            task.paras[i-1]=strA[i];
        return task;
        
    }
    
    
    public CmdTask addTask(String _name) {
        return this.addTask(_name, 1, 50, 0);
    }

    public CmdTask addTask(String _name, int _retryAmt, int _reTryDly) {
        return this.addTask(_name, _retryAmt, _reTryDly, 0);
    }

    public CmdTask addTask(String _name, int _retryAmt, int _reTryDly, int _firstDly) {
        CmdTask task = new CmdTask(_name);
        task.retryAmt = _retryAmt;
        task.retryDly = _reTryDly;
        task.retryTim = _reTryDly - _firstDly;
        taskMap.put(_name, task);
        return task;
    }

    public void addHoldKey(String taskName, String _holdKey) {
        int holdCnt;
        try{
            holdCnt = holdMap.get(_holdKey);
            holdCnt++;
        }
        catch(Exception ex){
            holdCnt=1;
            
        }
        holdMap.put(_holdKey, holdCnt);
        CmdTask task = taskMap.get(taskName);
        if (task != null) {
            task.holdKey = _holdKey;
        }
    }

    public int taskEnd(CmdTask task) {
        task.stepInx = 0;
        task.stepTim = 9999;
        task.retryTim = 0;
        task.retryCnt += 1;
        if (task.retryAmt > 0) {
            if (task.retryCnt >= task.retryAmt) {
                taskMap.remove(task.name);
                if (task.holdKey.length() > 0) {
                    int holdCnt = holdMap.get(task.holdKey);
                    holdCnt--;
                    if (holdCnt > 0) {
                        holdMap.put(task.holdKey, holdCnt);
                    } else {
                        taskMap.remove(task.holdKey);
                    }
                }
                return 1;
            }
        }
        return 0;
    }

}

abstract class TaskStackExe {

    public abstract String exe(CmdTask task);
}
