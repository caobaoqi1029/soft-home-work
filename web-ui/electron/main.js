const { app, BrowserWindow, ipcMain} = require('electron')
const path = require('node:path')
// 判断是否开发环境
const isPackaged = app.isPackaged

process.env['ELECTRON_DISABLE_SECURITY_WARNINGS'] = 'true'

var mainWindow

app.whenReady().then(() => {
    createMainWindow()
    app.on("activate", () => {
      if (BrowserWindow.getAllWindows().length === 0) createMainWindow()
    })
  })

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

/**创建主窗口 */
const createMainWindow = () => {
  mainWindow = new BrowserWindow({
    width: 880,
    height: 500,
    webPreferences:{
      nodeIntegration:true,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    }

  })
  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
  })



  if (!isPackaged){
    mainWindow.loadURL("http://localhost:5173/");
  }else{
    mainWindow.loadFile(path.resolve(__dirname, '../dist/index.html'))
  }

}


ipcMain.on('set-title', (event, title) => {
    const webContents = event.sender
    const win = BrowserWindow.fromWebContents(webContents)
    win.setTitle(title)
  })
