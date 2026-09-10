import { useDispatch, useSelector } from "react-redux";
import "./App.css";
import usePostFetch from "./hooks/usePostFetch";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Home from "./Home";
import Login from "./Login";
import Signup from "./Signup";
import { useEffect, useState } from "react";
import { setAuth } from "./store/AuthSlice";

function App() {
  const auth = useSelector((store) => store.auth);
  const { fetch } = usePostFetch();
  const [authChecked, setAuthChecked] = useState(false);
  const dispatch = useDispatch();

  useEffect(() => {
    const autoLogin = async () => {
      if (!auth.token) {
        setAuthChecked(true);
        return;
      }

      try {
        const data = await fetch("/autoLogin");

        if (data) {
          data.token = auth.token;
          dispatch(setAuth(data));
        }
      } finally {
        setAuthChecked(true);
      }
    };

    autoLogin();
  }, [auth.token]);

  if (!authChecked) {
    return (
      <div className="flex h-screen items-center text-3xl text-white font-semibold justify-center">
        Signing you in...
      </div>
    );
  }

  if (!authChecked) {
    return <div className="flex h-screen items-center text-3xl text-white font-semibold justify-center">
      Signing you in...
    </div>
  }

  return (
    <BrowserRouter>
      {auth.username !== null ? (
        <Routes>
          <Route path="/" element={<Home />}></Route>
          <Route path="*" element={<Navigate to={"/"} />}></Route>
        </Routes>
      ) : (
        <Routes>
          <Route path="/login" element={<Login />}></Route>
          <Route path="/signup" element={<Signup />}></Route>
          <Route path="*" element={<Navigate to={"/login"} />}></Route>
        </Routes>
      )}
    </BrowserRouter>
  );
}

export default App;
