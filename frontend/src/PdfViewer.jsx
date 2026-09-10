import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/Page/TextLayer.css";
import "react-pdf/dist/Page/AnnotationLayer.css";

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  "pdfjs-dist/build/pdf.worker.min.mjs",
  import.meta.url
).toString();

import { memo, useEffect, useState } from "react";
import { useSelector } from "react-redux";


function PdfViewer({ pdfUrl, width }) {
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [pageInput, setPageInput] = useState("1");

  const activeCitation = useSelector((state) => state.workspace.activeCitation);
  console.log("pdf")
  console.log(pdfUrl)
  console.log("pdf")

 

  useEffect(() => {
    if (activeCitation) {
      setPageNumber(activeCitation.pageNumber);
      setPageInput(String(activeCitation.pageNumber));
    }
  }, [activeCitation]);

  useEffect(() => {
    setPageInput(String(pageNumber));
  }, [pageNumber]);

  const handlePageInputChange = (e) => {
    setPageInput(e.target.value.replace(/\D/g, ""));
  };

  const handlePageInputEnter = (e) => {
    if (e.key !== "Enter") return;
    const value = parseInt(pageInput, 10);
    if (Number.isInteger(value) && value >= 1 && value <= numPages) {
      setPageNumber(value);
    }
  };

  const handlePageInputBlur = () => {
    setPageInput(String(pageNumber));
  };

  let token = localStorage.getItem("JwtToken")

  return (
    <div className="w-full h-full overflow-auto flex justify-center  relative">
      <Document
        file={{
          url: pdfUrl,
          httpHeaders: {
            Authorization: `Bearer ${token}`,
          },
        }}
        onLoadSuccess={({ numPages }) => setNumPages(numPages)}
      >
        <Page width={width} pageNumber={pageNumber} />
      </Document>

      <button
        className="absolute top-2 left-2 px-4 py-2 rounded-md bg-primary text-white font-semibold shadow hover:opacity-90 transition disabled:opacity-40 disabled:cursor-not-allowed"
        disabled={pageNumber <= 1}
        onClick={() => setPageNumber((p) => p - 1)}
      >
        Previous
      </button>

      <span className="absolute top-2 left-1/2 -translate-x-1/2 px-4 py-2 rounded-md bg-white border border-gray-300 text-gray-900 font-semibold shadow">
        Page{" "}
        <input
          value={pageInput}
          onChange={handlePageInputChange}
          onKeyDown={handlePageInputEnter}
          onBlur={handlePageInputBlur}
          className="w-12 text-center bg-transparent border-b border-primary text-primary font-bold outline-none"
        />{" "}
        / {numPages}
      </span>

      <button
        className="absolute top-2 right-2 px-4 py-2 rounded-md bg-primary text-white font-semibold shadow hover:opacity-90 transition disabled:opacity-40 disabled:cursor-not-allowed"
        disabled={pageNumber >= numPages}
        onClick={() => setPageNumber((p) => p + 1)}
      >
        Next
      </button>
    </div>
  );
}
export default memo(PdfViewer)