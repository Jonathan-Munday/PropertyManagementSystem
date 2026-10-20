import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, HashRouter } from "react-router-dom";
import App from "./App.tsx";
import { PortfolioProvider } from "./context/PortfolioContext.tsx";
import "./index.css";

const Router = import.meta.env.VITE_HASH_ROUTER === "true" ? HashRouter : BrowserRouter;

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Router>
      <PortfolioProvider>
        <App />
      </PortfolioProvider>
    </Router>
  </StrictMode>,
);
