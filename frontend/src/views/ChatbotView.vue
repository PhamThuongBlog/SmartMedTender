<template>
  <div class="page-container chatbot-page">
    <div class="chatbot-layout">
      <!-- FAQ Sidebar -->
      <div class="faq-sidebar">
        <h3 class="faq-title">Câu hỏi thường gặp</h3>
        <div v-if="loadingFaqs" class="loading-container">
          <ProgressSpinner style="width: 30px; height: 30px;" />
        </div>
        <div v-else class="faq-list">
          <div
            v-for="(faq, index) in faqs"
            :key="index"
            class="faq-item"
            @click="sendFaq(faq.question || faq)"
          >
            <i class="pi pi-question-circle"></i>
            <span>{{ faq.question || faq }}</span>
          </div>
        </div>
      </div>

      <!-- Chat Main Area -->
      <div class="chat-main">
        <div class="chat-header">
          <div class="flex-center gap-3">
            <div class="bot-avatar">
              <i class="pi pi-android"></i>
            </div>
            <div>
              <h2>Trợ lý AI MedTender</h2>
              <p class="online-status">Sẵn sàng hỗ trợ</p>
            </div>
          </div>
          <Button
            icon="pi pi-refresh"
            severity="secondary"
            text
            rounded
            v-tooltip.left="'Làm mới hội thoại'"
            @click="clearChat"
          />
        </div>

        <!-- Messages -->
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="messages.length === 0" class="chat-welcome">
            <i class="pi pi-comments" style="font-size: 3rem; color: var(--primary-color);"></i>
            <h3>Xin chào!</h3>
            <p>Tôi là trợ lý AI của MedTender. Tôi có thể giúp bạn:</p>
            <ul>
              <li>Tìm kiếm thông tin gói thầu</li>
              <li>Giải đáp quy trình đấu thầu</li>
              <li>Hướng dẫn sử dụng hệ thống</li>
              <li>Tư vấn về hồ sơ dự thầu</li>
            </ul>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-wrapper"
            :class="msg.role"
          >
            <div class="chat-message" :class="msg.role">
              <div class="message-content" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>

            <!-- Related Questions -->
            <div v-if="msg.relatedQuestions?.length" class="related-questions">
              <span class="related-label">Câu hỏi liên quan:</span>
              <div class="related-list">
                <Tag
                  v-for="(rq, idx) in msg.relatedQuestions"
                  :key="idx"
                  :value="rq.question || rq"
                  severity="info"
                  class="related-tag"
                  @click="sendFaq(rq.question || rq)"
                />
              </div>
            </div>
          </div>

          <!-- Typing indicator -->
          <div v-if="isTyping" class="message-wrapper bot">
            <div class="chat-message bot typing">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="chat-input-area">
          <div class="chat-input-wrapper">
            <Textarea
              v-model="inputMessage"
              placeholder="Nhập câu hỏi của bạn..."
              :rows="1"
              autoResize
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="isTyping"
              class="chat-input"
            />
            <Button
              icon="pi pi-send"
              severity="primary"
              rounded
              :disabled="!inputMessage.trim() || isTyping"
              @click="sendMessage"
              class="send-btn"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import apiClient from '@/api/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Tag from 'primevue/tag'
import ProgressSpinner from 'primevue/progressspinner'

const toast = useToast()

const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const faqs = ref([])
const loadingFaqs = ref(false)
const messagesContainer = ref(null)

async function fetchFaqs() {
  loadingFaqs.value = true
  try {
    const response = await apiClient.get('/chatbot/faqs')
    faqs.value = response.data || []
  } catch (error) {
    // Silently fail - FAQs are not critical
  } finally {
    loadingFaqs.value = false
  }
}

async function sendMessage() {
  const question = inputMessage.value.trim()
  if (!question || isTyping.value) return

  messages.value.push({
    role: 'user',
    content: question,
    timestamp: new Date().toISOString()
  })
  inputMessage.value = ''
  await scrollToBottom()

  isTyping.value = true
  try {
    const response = await apiClient.post('/chatbot/ask', { question })
    messages.value.push({
      role: 'bot',
      content: response.data.answer,
      confidence: response.data.confidence,
      relatedQuestions: response.data.relatedQuestions,
      timestamp: new Date().toISOString()
    })
    await scrollToBottom()
  } catch (error) {
    messages.value.push({
      role: 'bot',
      content: 'Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.',
      timestamp: new Date().toISOString()
    })
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể kết nối đến trợ lý AI', life: 5000 })
  } finally {
    isTyping.value = false
    await scrollToBottom()
  }
}

function sendFaq(question) {
  inputMessage.value = question
  sendMessage()
}

function clearChat() {
  messages.value = []
}

function formatMessage(text) {
  if (!text) return ''
  // Simple formatting for bold and line breaks
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  fetchFaqs()
})
</script>

<style scoped>
.chatbot-page {
  height: calc(100vh - var(--header-height) - 3rem);
  display: flex;
}

.chatbot-layout {
  display: flex;
  height: 100%;
  width: 100%;
  gap: 1.25rem;
}

.faq-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: var(--surface-card);
  border-radius: 12px;
  border: 1px solid var(--surface-border);
  padding: 1.25rem;
  overflow-y: auto;
}

.faq-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--surface-border);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.faq-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.625rem 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.faq-item:hover {
  background: var(--primary-light);
  color: var(--primary-color);
}

.faq-item i {
  color: var(--primary-color);
  margin-top: 0.125rem;
  flex-shrink: 0;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--surface-card);
  border-radius: 12px;
  border: 1px solid var(--surface-border);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--surface-border);
  background: var(--surface-ground);
}

.bot-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.bot-avatar i {
  font-size: 1.25rem;
  color: white;
}

.chat-header h2 {
  font-size: 1.0625rem;
  margin-bottom: 0;
}

.online-status {
  font-size: 0.75rem;
  color: var(--success-color);
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.chat-welcome {
  text-align: center;
  padding: 2rem;
  margin: auto;
}

.chat-welcome h3 {
  margin: 1rem 0 0.5rem;
}

.chat-welcome p {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.chat-welcome ul {
  text-align: left;
  display: inline-block;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.chat-welcome ul li {
  margin-bottom: 0.25rem;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 75%;
}

.message-wrapper.user {
  align-self: flex-end;
  align-items: flex-end;
}

.message-wrapper.bot {
  align-self: flex-start;
  align-items: flex-start;
}

.message-content {
  line-height: 1.5;
}

.message-time {
  font-size: 0.6875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.related-questions {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.related-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.related-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
}

.related-tag {
  cursor: pointer;
}

.related-tag:hover {
  opacity: 0.8;
}

.typing {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 1rem;
}

.typing .dot {
  width: 6px;
  height: 6px;
  background: var(--text-secondary);
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

.chat-input-area {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--surface-border);
  background: var(--surface-ground);
}

.chat-input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  background: var(--surface-card);
  border: 1px solid var(--surface-border);
  border-radius: 12px;
  padding: 0.5rem 0.75rem;
}

.chat-input {
  flex: 1;
  border: none;
  box-shadow: none;
}

.chat-input :deep(.p-inputtext) {
  border: none;
  box-shadow: none;
}

.send-btn {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .faq-sidebar {
    display: none;
  }

  .chatbot-layout {
    display: block;
  }
}
</style>
