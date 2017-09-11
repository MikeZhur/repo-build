package repo.build

import groovy.transform.CompileStatic


/**
 * @author Markelov Ruslan markelov@jet.msk.su
 */
@CompileStatic
class ActionContext implements Closeable {
    final ActionContext parent
    final RepoEnv env
    final String id
    final CliOptions options
    final List<ByteArrayOutputStream> processOutList = new ArrayList<>()
    final List<ActionContext> childList = new ArrayList<>()
    final ActionHandler actionHandler
    boolean output = false

    ActionContext(RepoEnv env, String id, CliOptions options, ActionHandler actionHandler1) {
        this.env = env
        this.id = id
        this.options = options
        this.actionHandler = actionHandler1
    }

    private ActionContext(ActionContext parent, String id) {
        this(parent.env, id, parent.options, parent.actionHandler)
        this.parent = parent
    }

    Closure newWriteOutHandler() {
        def out = new ByteArrayOutputStream();
        synchronized (processOutList) {
            processOutList.add(out)
        }
        return { int b ->
            out.write(b)
        }
    }

    void newChildWriteOut(String msg) {
        newChild('').writeOut(msg)
    }

    void writeOut(String msg) {
        if (msg != null) {
            def out = newWriteOutHandler();
            msg.getBytes("utf-8").each { out(it) }
        }
    }

    ActionContext newChild() {
        return newChild('')
    }

    ActionContext newChild(String id) {
        def child = new ActionContext(this, id)
        synchronized (childList) {
            childList.add(child)
        }
        actionHandler.beginAction(child)
        return child
    }

    void close() throws IOException {
        actionHandler.endAction(this)
    }

    int getParallel() {
        return options.getParallel(env)
    }
}
