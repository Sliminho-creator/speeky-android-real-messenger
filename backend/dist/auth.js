import jwt from "jsonwebtoken";
import { findUserById } from "./store.js";
const secret = process.env.JWT_SECRET || "change-me-please";
export function signToken(userId, deviceId) {
    return jwt.sign({ userId, deviceId }, secret, { expiresIn: "30d" });
}
export function requireAuth(req, res, next) {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : "";
    if (!token)
        return res.status(401).json({ error: "UNAUTHORIZED" });
    try {
        const decoded = jwt.verify(token, secret);
        const user = findUserById(decoded.userId);
        if (!user)
            return res.status(401).json({ error: "UNAUTHORIZED" });
        req.auth = decoded;
        next();
    }
    catch {
        return res.status(401).json({ error: "UNAUTHORIZED" });
    }
}
