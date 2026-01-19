<template>
  <div class="welcome-page">
    <div class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="main-title">Scientific Publications Platform</h1>
          <p class="tagline">Research Center XYZ</p>
          <p class="description">
            Manage and discover scientific publications across multiple research areas.
            Collaborate with your team, share insights, and stay updated on the latest research.
          </p>
          <div class="cta-buttons">
            <nuxt-link to="/auth/login" class="btn-login">
              Login to Platform
            </nuxt-link>
          </div>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2>Platform Features</h2>
        <div class="features-grid">
          <div class="feature-card">
            <h3>Publication Management</h3>
            <p>Upload and organize scientific publications, articles, conference papers, patents, datasets, and more.</p>
          </div>

          <div class="feature-card">
            <h3>Smart Tagging</h3>
            <p>Tag publications by project, area, or topic. Subscribe to tags and get notified of new additions.</p>
          </div>

          <div class="feature-card">
            <h3>Collaboration</h3>
            <p>Add comments, ratings, and critical analyses to publications. Share insights with your team.</p>
          </div>

          <div class="feature-card">
            <h3>Advanced Search</h3>
            <p>Search by title, author, scientific area, tags, date, and more. Filter and sort results easily.</p>
          </div>

          <div class="feature-card">
            <h3>AI-Powered Summaries</h3>
            <p>Automatically generate summaries of publications using advanced language models.</p>
          </div>

          <div class="feature-card">
            <h3>Real-time Notifications</h3>
            <p>Get email alerts when publications with your subscribed tags are added or updated.</p>
          </div>
        </div>
      </div>
    </div>

    <div class="research-areas">
      <div class="container">
        <h2>Research Areas</h2>
        <div class="areas-list">
          <span class="area-badge">Data Science</span>
          <span class="area-badge">Materials Science</span>
          <span class="area-badge">Artificial Intelligence</span>
          <span class="area-badge">Machine Learning</span>
          <span class="area-badge">Biotechnology</span>
          <span class="area-badge">Nanotechnology</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// No login logic needed here anymore
</script>

<style scoped>
.welcome-page {
  background: #36393f;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Hero Section */
.hero-section {
  background: #202225;
  padding: 120px 0;
}

.hero-content {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
  color: #dcddde;
}

.main-title {
  font-size: 56px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #ffffff;
}

.tagline {
  font-size: 24px;
  margin: 0 0 24px 0;
  color: #b9bbbe;
}

.description {
  font-size: 18px;
  line-height: 1.6;
  margin: 0 0 40px 0;
  color: #b9bbbe;
}

.cta-buttons {
  display: flex;
  justify-content: center;
}

.btn-login {
  padding: 16px 48px;
  background: #ffffff;
  color: #202225;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.2s;
  display: inline-block;
}

.btn-login:hover {
  background: #dcddde;
}

/* Features Section */
.features-section {
  padding: 80px 0;
}

.features-section h2 {
  text-align: center;
  font-size: 32px;
  color: #ffffff;
  margin: 0 0 50px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.feature-card {
  background: #2f3136;
  padding: 24px;
  border-radius: 8px;
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-card h3 {
  font-size: 20px;
  color: #ffffff;
  margin: 0 0 12px 0;
}

.feature-card p {
  font-size: 14px;
  color: #b9bbbe;
  line-height: 1.6;
  margin: 0;
}

/* Research Areas */
.research-areas {
  background: #2f3136;
  padding: 60px 0;
}

.research-areas h2 {
  font-size: 28px;
  color: #ffffff;
  margin: 0 0 30px 0;
  text-align: center;
}

.areas-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.area-badge {
  background: #202225;
  color: #b9bbbe;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-section {
    padding: 80px 0;
  }

  .main-title {
    font-size: 36px;
  }

  .tagline {
    font-size: 20px;
  }

  .description {
    font-size: 16px;
  }

  .features-section {
    padding: 60px 0;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>