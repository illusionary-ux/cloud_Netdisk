<template>
  <el-dialog
    v-model="dialogVisible"
    :title="fileName"
    width="70%"
    :before-close="handleClose"
    class="preview-dialog"
  >
    <div class="preview-container" v-loading="loading">
      <!-- 图片预览 -->
      <div v-if="isImage" class="image-preview">
        <img :src="fileUrl" :alt="fileName" />
      </div>

      <!-- 音频预览 -->
      <div v-else-if="isAudio" class="audio-preview">
        <audio controls class="audio-player" @error="handleMediaError" @loadstart="handleMediaLoadStart" @canplay="handleMediaCanPlay">
          <source :src="fileUrl" :type="getMimeType(fileType)" />
          您的浏览器不支持音频预览
        </audio>
      </div>

      <!-- 视频预览 -->
      <div v-else-if="isVideo" class="video-preview">
        <video 
          controls 
          class="video-player" 
          @error="handleMediaError" 
          @loadstart="handleMediaLoadStart" 
          @canplay="handleMediaCanPlay"
          :src="fileUrl"
          preload="auto"
          poster=""
        ></video>
        <!-- 添加直接下载选项 -->
        <div class="video-fallback">
          <p>如果视频无法播放，您可以尝试:</p>
          <el-button type="primary" @click="downloadFile">下载视频</el-button>
          <el-button @click="openInNewTab">在新窗口中打开</el-button>
          <el-button type="success" @click="downloadRawVideo">下载原始文件</el-button>
        </div>
      </div>

      <!-- PDF预览 -->
      <div v-else-if="isPdf" class="pdf-preview">
        <iframe :src="pdfViewerUrl" width="100%" height="600"></iframe>
      </div>

      <!-- Office文档预览 -->
      <div v-else-if="isOffice" class="office-preview">
        <iframe :src="officeViewerUrl" width="100%" height="600"></iframe>
      </div>

      <!-- 不支持预览的文件类型 -->
      <div v-else class="unsupported-preview">
        <el-empty description="当前文件类型暂不支持预览，请下载后查看">
          <el-button type="primary" @click="downloadFile">下载文件</el-button>
        </el-empty>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getPreviewInfo } from '@/api/file';

const props = defineProps({
  fileId: {
    type: String,
    required: true
  },
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);

const dialogVisible = ref(props.visible);
const loading = ref(true);
const fileInfo = ref({});
const fileName = ref('');
const fileUrl = ref('');
const fileType = ref('');

// 添加视频播放状态
const isVideoPlaying = ref(false);

// 监听visible属性变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal;
  if (newVal && props.fileId) {
    loadFileInfo(props.fileId);
  }
});

// 监听对话框关闭
watch(() => dialogVisible.value, (newVal) => {
  if (!newVal) {
    emit('close');
  }
});

// 获取文件信息
const loadFileInfo = async (fileId) => {
  loading.value = true;
  try {
    const res = await getPreviewInfo(fileId);
    console.log('文件预览信息响应:', res);
    if (res.code === 1) {
      fileInfo.value = res.data;
      fileName.value = res.data.fileName;
      fileUrl.value = res.data.fileUrl;
      fileType.value = res.data.fileType;
      console.log('文件URL:', fileUrl.value);
      console.log('文件类型:', fileType.value);
      console.log('MIME类型:', getMimeType(fileType.value));

      // 如果是视频文件，提供更多选项
      if (res.data.isVideo) {
        isVideoPlaying.value = false;
      }
    } else {
      ElMessage.error(res.msg || '获取文件预览信息失败');
    }
  } catch (error) {
    console.error('获取文件预览信息失败', error);
    ElMessage.error('获取文件预览信息失败');
  } finally {
    loading.value = false;
  }
};

// 判断文件类型
const isImage = computed(() => {
  return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(fileType.value);
});

const isAudio = computed(() => {
  return /\.(mp3|wav|ogg|flac|aac)$/i.test(fileType.value);
});

const isVideo = computed(() => {
  // 增加更多视频格式支持
  return /\.(mp4|webm|ogg|mov|avi|mkv|flv|wmv|m4v|mpg|mpeg)$/i.test(fileType.value);
});

const isPdf = computed(() => {
  return /\.pdf$/i.test(fileType.value);
});

const isOffice = computed(() => {
  return /\.(doc|docx|xls|xlsx|ppt|pptx)$/i.test(fileType.value);
});

// PDF预览URL（使用在线PDF查看器）
const pdfViewerUrl = computed(() => {
  if (!isPdf.value) return '';
  // 直接使用Google PDF Viewer
  return `https://docs.google.com/viewer?url=${encodeURIComponent(fileUrl.value)}&embedded=true`;
});

// Office文档预览URL
const officeViewerUrl = computed(() => {
  if (!isOffice.value) return '';
  if (fileInfo.value.previewUrl) {
    return fileInfo.value.previewUrl;
  }
  // 使用Microsoft Office Online Viewer
  return `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(fileUrl.value)}`;
});

// 下载文件
const downloadFile = async () => {
  if (isVideo.value) {
    // 对于视频文件，获取最新的签名URL
    const videoUrl = await getVideoUrl();
    // 创建下载链接
    const a = document.createElement('a');
    a.href = videoUrl;
    a.download = fileName.value || '视频文件'; // 设置下载文件名
    a.target = '_blank';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  } else {
    // 非视频文件使用原有方法
    window.open(fileUrl.value, '_blank');
  }
};

// 直接从服务器下载原始视频文件
const downloadRawVideo = () => {
  if (!props.fileId) return;
  // 生成直接下载链接
  const downloadUrl = `/api/files/${props.fileId}/raw`;
  window.open(downloadUrl, '_blank');
};

const handleClose = () => {
  dialogVisible.value = false;
  emit('close');
};

// 获取正确的MIME类型
const getMimeType = (fileType) => {
  if (!fileType) return '';
  
  // 处理音频格式
  if (fileType.match(/\.mp3$/i)) return 'audio/mpeg';
  if (fileType.match(/\.wav$/i)) return 'audio/wav';
  if (fileType.match(/\.ogg$/i)) return 'audio/ogg';
  if (fileType.match(/\.flac$/i)) return 'audio/flac';
  if (fileType.match(/\.aac$/i)) return 'audio/aac';
  
  // 处理视频格式
  if (fileType.match(/\.mp4$/i)) return 'video/mp4';
  if (fileType.match(/\.webm$/i)) return 'video/webm';
  if (fileType.match(/\.ogg$/i)) return 'video/ogg';
  if (fileType.match(/\.mov$/i)) return 'video/quicktime';
  if (fileType.match(/\.avi$/i)) return 'video/x-msvideo';
  if (fileType.match(/\.mkv$/i)) return 'video/x-matroska';
  
  return '';
};

// 媒体加载相关事件处理
const handleMediaError = (event) => {
  console.error('媒体加载错误:', event);
  console.error('错误代码:', event.target.error ? event.target.error.code : '未知');
  console.error('错误详情:', event.target.error);
  ElMessage.error('媒体文件加载失败，可能是格式不支持或者跨域问题');
  isVideoPlaying.value = false;
};

const handleMediaLoadStart = () => {
  console.log('媒体开始加载');
};

const handleMediaCanPlay = () => {
  console.log('媒体可以播放');
  isVideoPlaying.value = true;
};

// 获取最新的视频下载URL（确保URL是最新的签名版本）
const getVideoUrl = async () => {
  try {
    // 重新获取预览信息，确保URL是最新的签名版本
    const res = await getPreviewInfo(props.fileId);
    if (res.code === 1) {
      return res.data.fileUrl;
    }
    return fileUrl.value; // 如果失败，返回当前URL
  } catch (error) {
    console.error('获取视频URL失败', error);
    return fileUrl.value; // 如果失败，返回当前URL
  }
};

// 在新窗口打开视频
const openInNewTab = async () => {
  // 获取最新的视频URL
  const videoUrl = await getVideoUrl();
  // 使用专用的视频播放页面而不是直接打开URL
  const videoPlayerUrl = `/video-player.html?url=${encodeURIComponent(videoUrl)}&title=${encodeURIComponent(fileName.value)}`;
  window.open(videoPlayerUrl, '_blank');
};

// 初始加载
if (props.fileId && props.visible) {
  loadFileInfo(props.fileId);
}
</script>

<style scoped>
.preview-dialog :deep(.el-dialog__body) {
  padding: 10px;
}

.preview-container {
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-preview {
  text-align: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 70vh;
}

.audio-preview {
  width: 100%;
  padding: 20px 0;
}

.audio-player {
  width: 100%;
}

.video-preview {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.video-player {
  width: 100%;
  max-height: 70vh;
  background-color: #000;
}

.video-fallback {
  margin-top: 15px;
  text-align: center;
  padding: 10px;
  border-top: 1px solid #eee;
}

.video-fallback p {
  margin-bottom: 10px;
  color: #666;
}

.pdf-preview,
.office-preview {
  width: 100%;
  height: 70vh;
}

.unsupported-preview {
  padding: 30px;
  text-align: center;
}
</style> 