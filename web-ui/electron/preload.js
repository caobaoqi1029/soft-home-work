const { contextBridge, ipcRenderer, shell } = require( "electron" );

contextBridge.exposeInMainWorld("electronAPI", {
    setTitle: (title) => ipcRenderer.send('set-title', title)
})