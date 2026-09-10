import { configureStore } from "@reduxjs/toolkit";
import authSlice from "./AuthSlice"
import workspaceSlice from "./WorkspaceSlice"
export const store = configureStore({
    reducer:{
        auth: authSlice,
        workspace: workspaceSlice
    }
})

