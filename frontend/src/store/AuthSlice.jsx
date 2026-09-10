import { createSlice } from "@reduxjs/toolkit";


const authSlice = createSlice({
    name:"authentication",
    initialState:{
        username:null,
        email:null,
        token: localStorage.getItem("JwtToken") || null,
        id: null
    },
    reducers:{
        setAuth: (state, action)=>{
            state.username = action.payload.username
            state.email = action.payload.email
            state.token = action.payload.token
            state.id = action.payload.id
            localStorage.setItem("JwtToken", action.payload.token)
        },
        updateProfile: (state, action)=>{
            state.username = action.payload.username;
        }
        ,
        clearAuth:(state, action)=>{
            state.username = null
            state.email = null
            state.token = null
            localStorage.removeItem("JwtToken")
        }
    }
})

export const {setAuth, clearAuth, updateProfile} = authSlice.actions;
export default authSlice.reducer;