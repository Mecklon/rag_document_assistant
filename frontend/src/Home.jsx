import React, { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { clearAuth } from "./store/AuthSlice";
import { FaRegPlusSquare } from "react-icons/fa";
import {
  addMessage,
  promptMessage,
  setActiveCitation,
  resetWorkspace,
} from "./store/WorkspaceSlice";
import { MdSend } from "react-icons/md";
import { AnimatePresence, motion } from "motion/react";
import { IoChevronForward } from "react-icons/io5";
import {
  fetchWorkspaces,
  uploadDocument,
  fetchWorkspaceDetails,
  selectWorkspace,
} from "./store/WorkspaceSlice";
import { MdAssistant } from "react-icons/md";
import { IoExitOutline } from "react-icons/io5";
import { FiUpload } from "react-icons/fi";
import PdfViewer from "./PdfViewer";
import rolling from "./assets/rolling.gif";

function Home() {
  const dispatch = useDispatch();
  const handleLogout = () => {
    dispatch(clearAuth());
  };

  const data = useSelector((store) => store.workspace);
  const [file, setFile] = useState(null);
  const messageInputRef = useRef(null);
  const fileInputRef = useRef(null);
  console.log(data);
  useEffect(() => {
    dispatch(fetchWorkspaces());
  }, []);

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (selected && selected.type === "application/pdf") {
      setFile(selected);
    } else {
      setFile(null);
    }
  };

  const handleUpload = () => {
    if (!file) return;
    dispatch(uploadDocument({ workspaceId: data.selectedWorkspaceId, file }));
    setFile(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleSelectWorkspace = (item, index) => {
    dispatch(selectWorkspace({ id: item.id, index }));
    dispatch(fetchWorkspaceDetails(item.id));
  };

  const handleNewWorkspace = () => {
    dispatch(resetWorkspace());
    setFile(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const setCitation = (citation) => {
    dispatch(
      setActiveCitation({
        documentId: citation.documentId,
        pageNumber: citation.pageNumber,
        text: citation.text,
      }),
    );
  };

  const handleSend = () => {
    if (data.retreivingPromptResponse) return;
    const content = messageInputRef.current.value.trim();
    if (!content) return;
    dispatch(
      addMessage({
        id: crypto.randomUUID(),
        content,
        role: null,
        createdAt: null,
        workspaceId: null,
        citations: [],
      }),
    );
    dispatch(promptMessage({ prompt: content }));
    messageInputRef.current.value = "";
  };
  console.log(data);
  const chatContainer = useRef();
  const messagesContainer = useRef();

  useEffect(() => {
    if (!messagesContainer.current) return;
    messagesContainer.current.scrollTop =
      messagesContainer.current.scrollHeight;
  }, [data.messages]);

  // const pdfUrl = data.selectedDocumentId
  //   ? `http://localhost:9090/documents/${data.selectedDocumentId}/file`
  //   : null;
  // console.log(data);

  const pdfUrl = data.selectedDocumentId
    ? `https://rag-document-assistant-osgo.onrender.com/${data.selectedDocumentId}/file`
    : null;
  console.log(data);

  const containerRef = useRef(null);
  const [width, setWidth] = useState(0);

  useEffect(() => {
    if (data.selectedDocumentId === null) return;
    const observer = new ResizeObserver(([entry]) => {
      setWidth(entry.contentRect.width);
    });
    observer.observe(containerRef.current);

    return () => observer.disconnect();
  }, [data.selectedDocumentId]);

  return (
    <div className="h-screen flex bg-gray-100 text-gray-900">
      <div className="border-r border-gray-300 p-3 w-70 shrink-0 flex flex-col gap-2 bg-white">
        <div className="flex items-center shrink-0 gap-2 pb-3 border-b border-gray-200">
          <MdAssistant className="text-4xl text-primary" />
          <div className="text-xl font-bold text-primary">
            Document Assistant
          </div>
        </div>
        <div className="flex gap-2 items-center bg-gray-100 rounded-md p-2 shrink-0">
          <div className="flex flex-col grow min-w-0">
            <div className="text-base font-bold text-primary">
              Mecklon Fernandes
            </div>
            <div className="text-xs text-gray-500 truncate">
              fernandesmecklon3@gmail.com
            </div>
          </div>
          <button
            onClick={handleLogout}
            title="Logout"
            className="shrink-0 text-primary hover:text-red-700 transition"
          >
            <IoExitOutline className="text-3xl" />
          </button>
        </div>
        <div className="flex items-center justify-between mt-2 shrink-0 px-1">
          <div className="text-sm font-bold text-primary uppercase tracking-wide">
            Workspaces
          </div>
          <button
            onClick={handleNewWorkspace}
            title="New workspace"
            className="shrink-0 text-primary hover:text-opacity-70 transition"
          >
            <FaRegPlusSquare className="text-2xl" />
          </button>
        </div>
        <div className="grow mt-1 noScrollBar gap-1.5 flex flex-col overflow-auto min-h-0 pb-1">
          {data.workspaces.map((item, index) => {
            const active = index === data.selectedWorkspaceIndex;
            return (
              <div
                key={item.id}
                onClick={() => handleSelectWorkspace(item, index)}
                className={`px-3 py-2 text-base font-semibold rounded-md cursor-pointer border transition ${
                  active
                    ? "bg-primary border-primary text-white shadow-lg"
                    : "bg-white border-gray-300 text-gray-700 hover:bg-primary/10 hover:border-primary hover:text-primary"
                }`}
              >
                {item.name}
              </div>
            );
          })}
        </div>
      </div>
      <div className="flex-1 min-w-0 border-l border-gray-300">
        {data.selectedWorkspaceId === null ? (
          <div className="h-full flex flex-col items-center justify-center gap-4 p-6">
            <MdAssistant className="text-6xl text-primary" />
            <div className="text-2xl font-bold text-primary">
              Upload a PDF document
            </div>
            <div className="text-lg text-gray-500">
              Select a workspace from the sidebar to begin
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="application/pdf"
              onChange={handleFileChange}
              className="hidden"
            />
            <button
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-2 px-6 py-3 rounded-lg bg-primary text-white font-semibold hover:opacity-90 transition shadow-lg"
            >
              <FiUpload className="text-xl" />
              {file ? file.name : "Choose a PDF file"}
            </button>
            {file && (
              <button
                onClick={handleUpload}
                disabled={data.uploadingDocument}
                className="px-6 py-2 rounded-lg border-2 border-primary bg-white text-primary font-semibold hover:bg-primary hover:text-white transition disabled:opacity-50"
              >
                {data.uploadingDocument ? "Uploading..." : "Upload"}
              </button>
            )}
          </div>
        ) : (
          <div
            ref={chatContainer}
            className="relative h-full flex pb-17 p-3 flex-col justify-end"
          >
            <div className="absolute left-3 top-2 font-bold text-xl text-primary">
              {data.workspaces[data.selectedWorkspaceIndex].name}
            </div>
            <div
              ref={messagesContainer}
              className="flex-1 min-h-0 overflow-y-auto noScrollBar flex flex-col pt-10"
            >
              <div className="mt-auto w-full flex flex-col gap-2">
                
                <AnimatePresence>
                  {data.messages.map((message) => {
                    const isUser =
                      message.role === "USER" || message.role === null;
                    return (
                      <motion.div
                        key={message.id}
                        className={`px-4 max-w-[60%] py-2 rounded-2xl w-fit relative shadow-sm ${
                          isUser
                            ? "self-end bg-primary text-white rounded-tr-sm"
                            : "self-start bg-white border border-gray-300 text-gray-900 rounded-tl-sm"
                        }`}
                        initial={{ scale: 0, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        layout
                        transition={{ duration: 0.3 }}
                      >
                        {message.content}
                        {message.citations.length > 0 && (
                          <div className="absolute top-[100%] mt-1 flex flex-col left-0 right-0 py-1 px-1 gap-0.5">
                            {message.citations.map((citation, index) => (
                              <div
                                key={index}
                                onClick={() => setCitation(citation)}
                                className="text-primary flex cursor-pointer items-center justify-between bg-primary/10 rounded-md px-2 py-1 hover:bg-primary/20 transition active:bg-primary/30"
                              >
                                <span>Citation {index+1}</span>
                                <IoChevronForward />
                              </div>
                            ))}
                          </div>
                        )}
                      </motion.div>
                    );
                  })}
                </AnimatePresence>
                {data.retreivingPromptResponse && (
                  <div className="flex items-center gap-2 self-start bg-primary text-white rounded-2xl px-4 py-2 text-lg w-fit shadow-sm">
                    <img src={rolling} alt="loading" className="h-6 w-6" />
                    Generating response...
                  </div>
                )}
              </div>
            </div>
            <div className="absolute bottom-2 items-end justify-center gap-1 left-2 p-2 right-2 rounded-2xl bg-white border border-gray-300 shadow-lg flex">
              <textarea
                rows={1}
                ref={messageInputRef}
                disabled={data.retreivingPromptResponse}
                onChange={(e) => {
                  e.target.style.height = "auto";
                  e.target.style.height = `${e.target.scrollHeight}px`;
                }}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    handleSend();
                  }
                }}
                name=""
                className="pl-2 self-center grow text-xl outline-0 noScrollBar text-left resize-none bg-transparent"
                id=""
              ></textarea>

              <button
                onClick={handleSend}
                disabled={data.retreivingPromptResponse}
                className="shrink-0 flex items-center justify-center bg-primary h-12 aspect-square rounded-full hover:opacity-90 transition disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <MdSend className="text-white text-3xl" />
              </button>
            </div>
          </div>
        )}
      </div>
      {data.selectedDocumentId != null && (
        <div
          ref={containerRef}
          className="border-l border-gray-300 flex-1 min-w-0 justify-center item-center flex bg-white"
        >
          <PdfViewer pdfUrl={pdfUrl} width={width} />
        </div>
      )}
    </div>
  );
}

export default Home;
