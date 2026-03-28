import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { api } from "../lib/api";

interface Profile { id: number; name: string; email: string; role: string; }

export function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [profileMsg, setProfileMsg] = useState<{ text: string; ok: boolean } | null>(null);
  const [passwordMsg, setPasswordMsg] = useState<{ text: string; ok: boolean } | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);

  useEffect(() => {
    api.get("/auth/me").then((res) => {
      setProfile(res.data);
      setName(res.data.name);
      setEmail(res.data.email);
    }).catch(() => {});
  }, []);

  async function saveProfile() {
    setProfileLoading(true);
    setProfileMsg(null);
    try {
      const res = await api.put("/auth/me", { name, email });
      setProfile(res.data);
      setProfileMsg({ text: "Profile updated successfully", ok: true });
    } catch {
      setProfileMsg({ text: "Failed to update profile", ok: false });
    } finally {
      setProfileLoading(false);
    }
  }

  async function changePassword() {
    if (!currentPassword || !newPassword) {
      setPasswordMsg({ text: "Both fields are required", ok: false });
      return;
    }
    if (newPassword.length < 8) {
      setPasswordMsg({ text: "New password must be at least 8 characters", ok: false });
      return;
    }
    setPasswordLoading(true);
    setPasswordMsg(null);
    try {
      await api.put("/auth/password", { currentPassword, newPassword });
      setPasswordMsg({ text: "Password changed successfully", ok: true });
      setCurrentPassword("");
      setNewPassword("");
    } catch {
      setPasswordMsg({ text: "Failed — check your current password", ok: false });
    } finally {
      setPasswordLoading(false);
    }
  }

  return (
    <section>
      <div className="page-head">
        <h2>My Profile</h2>
        <p>Manage your account details and security settings.</p>
      </div>

      <div className="card-grid">
        {/* Avatar + info */}
        <motion.article className="card" style={{ gridColumn: "span 4" }}
          initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
          <div className="profile-avatar-block">
            <div className="profile-avatar">
              {profile?.name?.charAt(0).toUpperCase() ?? "?"}
            </div>
            <div>
              <strong>{profile?.name ?? "Loading..."}</strong>
              <p className="muted-line">{profile?.email}</p>
              <span className={`status-badge ${profile?.role === "ADMIN" ? "badge-booked" : "badge-delivered"}`}>
                {profile?.role ?? "CUSTOMER"}
              </span>
            </div>
          </div>
        </motion.article>

        {/* Edit profile */}
        <motion.article className="card" style={{ gridColumn: "span 8" }}
          initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
          <h3>Edit Profile</h3>
          <div className="form-grid">
            <div>
              <label htmlFor="prof-name">Full Name</label>
              <input id="prof-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="Your name" />
            </div>
            <div>
              <label htmlFor="prof-email">Email</label>
              <input id="prof-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="your@email.com" />
            </div>
          </div>
          {profileMsg && (
            <p className={profileMsg.ok ? "msg-ok" : "alert"} style={{ marginTop: "10px" }}>{profileMsg.text}</p>
          )}
          <div style={{ marginTop: "14px" }}>
            <button type="button" className="btn-primary-sm" onClick={saveProfile} disabled={profileLoading}>
              {profileLoading ? "Saving..." : "Save Changes"}
            </button>
          </div>
        </motion.article>

        {/* Change password */}
        <motion.article className="card" style={{ gridColumn: "span 12" }}
          initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
          <h3>Change Password</h3>
          <div className="form-grid">
            <div>
              <label htmlFor="cur-pass">Current Password</label>
              <input id="cur-pass" type="password" value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)} placeholder="Enter current password" />
            </div>
            <div>
              <label htmlFor="new-pass">New Password</label>
              <input id="new-pass" type="password" value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)} placeholder="Minimum 8 characters" />
            </div>
          </div>
          {passwordMsg && (
            <p className={passwordMsg.ok ? "msg-ok" : "alert"} style={{ marginTop: "10px" }}>{passwordMsg.text}</p>
          )}
          <div style={{ marginTop: "14px" }}>
            <button type="button" className="btn-primary-sm" onClick={changePassword} disabled={passwordLoading}>
              {passwordLoading ? "Updating..." : "Update Password"}
            </button>
          </div>
        </motion.article>
      </div>
    </section>
  );
}
