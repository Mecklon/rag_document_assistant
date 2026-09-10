import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import api from "../api/api";

export const fetchWorkspaces = createAsyncThunk(
  "workspace/fetchWorkspaces",
  async (_, thunkAPI) => {
    const response = await api.get("/workspaces");
    return response.data;
  },
);

export const uploadDocument = createAsyncThunk(
  "workspace/uploadDocument",
  async ({ workspaceId, file }, thunkAPI) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append(
      "request",
      new Blob([JSON.stringify({ workspaceId })], { type: "application/json" }),
    );
    console.log("1");
    const response = await api.post("/addDocument", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    console.log("2");
    return response.data;
  },
);

export const promptMessage = createAsyncThunk(
  "workspace/promptMessage",
  async ({prompt}, thunkAPI)=>{
    console.log("workspace id")
    console.log(thunkAPI.getState().workspace.selectedWorkspaceId)
    const response = await api.post("/prompt",{
      prompt,
      workspaceId: thunkAPI.getState().workspace.selectedWorkspaceId
    })
    return response.data;
  }
)

export const fetchWorkspaceDetails = createAsyncThunk(
  "workspace/fetchWorkspaceDetails",
  async (workspaceId, thunkAPI) => {
    const response = await api.get(`/getWorkspaceDetails?workspaceId=${workspaceId}`);
    return response.data;
  },
);

const workspaceSlice = createSlice({
  name: "workspace",
  initialState: {
    workspaces: [],
    selectedWorkspaceId: null,
    selectedWorkspaceIndex: null,
    selectedDocumentId: null,
    activeCitation: null,
    loadingWorkspaces: false,
    uploadingDocument: false,
    documents: [],
    messages: [],
    retreivingPromptResponse:false
  },
  reducers: {
    selectWorkspace(state, action) {
      state.selectedWorkspaceId = action.payload.id;
      state.selectedWorkspaceIndex = action.payload.index;
    },
    setActiveCitation(state, action){
        state.activeCitation = action.payload
    },
    addMessage(state, action){
      console.log("message added")
      state.messages = [...state.messages,action.payload]
    },
    resetWorkspace(state, action){
      state.selectedWorkspaceId = null;
      state.selectedWorkspaceIndex = null;
      state.selectedDocumentId = null;
      state.activeCitation = null;
      state.documents = [];
      state.messages = [];
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchWorkspaces.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchWorkspaces.fulfilled, (state, action) => {
        state.loading = false;
        state.workspaces = [...action.payload].reverse();
      })
      .addCase(fetchWorkspaces.rejected, (state, action) => {
        state.loading = false;
        //state.error = action.error.message;
      })
      .addCase(promptMessage.pending, (state) => {
        state.retreivingPromptResponse = true;
      })
      .addCase(promptMessage.fulfilled, (state, action) => {
        state.retreivingPromptResponse = false;
        state.messages = [...state.messages, action.payload]
      })
      .addCase(promptMessage.rejected, (state, action) => {
        state.retreivingPromptResponse = false;
        //state.error = action.error.message;
      })

      .addCase(uploadDocument.pending, (state) => {
        state.uploadingDocument = true;
      })
      .addCase(uploadDocument.fulfilled, (state, action) => {
        state.uploadingDocument = false;
        const updated = action.payload;
        const index = state.workspaces.findIndex((w) => w.id === updated.id);
        if (index !== -1) {
          state.workspaces[index] = updated;
        } else {
          state.workspaces.unshift(updated);
        }
        state.selectedWorkspaceId = updated.id;
        state.selectedWorkspaceIndex = index !== -1 ? index : 0;
        state.documents = updated.document;
        state.messages = updated.messages; 
        state.selectedDocumentId = updated.document[0].id;
      })
      .addCase(uploadDocument.rejected, (state) => {
        state.uploadingDocument = false;
      })
      .addCase(fetchWorkspaceDetails.pending, (state) => {
        state.loadingWorkspaces = true;
      })
      .addCase(fetchWorkspaceDetails.fulfilled, (state, action) => {
        state.loadingWorkspaces = false;
        console.log("fa;djf;a")
        const details = action.payload;
        console.log(details)
        state.documents = details.document;
        state.messages = details.messages;
        state.selectedDocumentId = details.document[0].id;
      })
      .addCase(fetchWorkspaceDetails.rejected, (state) => {
        state.loadingWorkspaces = false;
      });
  },
});

export const { selectWorkspace ,setActiveCitation, addMessage, resetWorkspace} = workspaceSlice.actions;
export default workspaceSlice.reducer;
